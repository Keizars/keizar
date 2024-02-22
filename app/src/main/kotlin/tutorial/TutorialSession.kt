package org.keizar.android.tutorial

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.keizar.android.tutorial.TutorialPresentation.Companion.DEFAULT_BUTTON_NAME
import org.keizar.game.GameSession
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.coroutines.childSupervisorScope
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration


/**
 * A session of a tutorial, which tracks the current progress of the player playing the tutorial.
 *
 * This session is thread-safe.
 */
interface TutorialSession {
    val tutorial: Tutorial

    /**
     * The game session associated with this tutorial session.
     *
     * You can perform moves using this game session, only if the current step requests the player to move.
     * See [presentation] for allowed actions.
     */
    val game: GameSession

    /**
     * The current step that the player is playing.
     * Initially the first step.
     */
    val currentStep: StateFlow<StepSession>

    /**
     * Actions that the [currentStep] is requesting. For example, requesting the player to move a piece.
     */
    val requests: TutorialRequests

    /**
     * UI components to be displayed to the user.
     */
    val presentation: TutorialPresentation

    /**
     * Revokes the current **and** the last step.
     * The tutorial will be returned to the state when the player is just started playing the last step.
     *
     * Example timeline:
     * ```
     * // Before [revoke]
     * 1 -> 2 -> 3 -> 4 -> 5 -> 6
     *                     ^
     *              currently playing
     *
     * // After [revoke]
     * 1 -> 2 -> 3 -> 4 -> 5 -> 6
     *                ^
     *         currently playing
     * ```
     */
    suspend fun back()

    /**
     * Starts the tutorial.
     *
     * Does nothing if the tutorial has already been started.
     */
    suspend fun start()

    suspend fun awaitSuccess()
}

private class Revoker(
    val name: String,
    val action: suspend () -> Unit,
)

private class Revokers {
    val revokers = mutableListOf<Revoker>()

    fun add(name: String, action: suspend () -> Unit) {
        revokers.add(Revoker(name, action))
    }

    fun clear() {
        revokers.clear()
    }
}

/**
 * @see TutorialSession
 */
internal class TutorialSessionImpl(
    override val tutorial: Tutorial,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
) : TutorialSession {
    private val tutorialScope = CoroutineScope(parentCoroutineContext + SupervisorJob(parentCoroutineContext[Job]))
    private val started = atomic(false)
    override val game = GameSession.restore(tutorial.initialGameSnapshot)

    private val stepSessions = tutorial.steps.mapIndexed { index, step ->
        StepSessionImpl(index = index, step = step)
    }

    override val currentStep: MutableStateFlow<StepSessionImpl> = MutableStateFlow(stepSessions.first())
    override val requests = TutorialRequestsImpl()
    override val presentation = TutorialPresentationImpl()

    private val executionLock = Mutex()
    private var currentLoopJob: Job? = null
    private val onSuccess = CompletableDeferred<Unit>()

    private val logger = logger("TutorialSessionImpl")

    override suspend fun back() = executionLock.withLock {
        check(started.value) { "Cannot revoke steps when the tutorial has not yet started" }

        this.currentLoopJob?.run {
            cancel(CancellationException("Revoking tutorial due to `back`"))
            join()
        }
        this.currentLoopJob = null

        val indexToRestart = getStepIndexToRestart()
        logger.info { "Back: restoring to savepoint ${stepSessions[indexToRestart].step.name} (index=$indexToRestart)" }

        for (i in currentStep.value.index downTo indexToRestart) {
            stepSessions[i].revoke()
        }

        // Start again
        launchSequentialExecution(startIndex = indexToRestart, invokeSavepoint = true)
    }

    override suspend fun start() = executionLock.withLock {
        if (!started.compareAndSet(expect = false, update = true)) {
            return
        }

        launchSequentialExecution(startIndex = 0, invokeSavepoint = false)
    }

    override suspend fun awaitSuccess() {
        onSuccess.await()
    }

    private fun getStepIndexToRestart(): Int {
        val lastInvoked = stepSessions.indexOfLast {
            val currentStep = currentStep.value
            if (currentStep.step is Savepoint && currentStep.index == it.index) { // current is savepoint, don't restart from current position (no effect)
                false
            } else {
                it.state.value.hasEffect
            }
        }
        if (lastInvoked == -1) {
            return 0
        }
        return stepSessions.asSequence()
            .take(lastInvoked + 1)
            .lastOrNull { it.step is Savepoint }
            ?.index
            ?: 0
    }

    private fun launchSequentialExecution(
        startIndex: Int,
        invokeSavepoint: Boolean
    ) {
        check(currentLoopJob == null)

        var savepointInvoked = false
        currentLoopJob = tutorialScope.launch {
            for (i in startIndex..stepSessions.lastIndex) {
                val session = stepSessions[i]
                logger.info { "Loop: current step '${session.step.name}' (index=${session.index})" }
                currentStep.value = session
                if (session.step !is Savepoint // a normal Action
                    || (invokeSavepoint && !savepointInvoked) // not yet invoked any savepoint
                ) {
                    savepointInvoked = true
                    try {
                        session.invoke()
                    } catch (e: CancellationException) {
                        break
                    }
                } else {
                    session.skip()
                }
            }
            onSuccess.complete(Unit)
        }
    }

    inner class TutorialPresentationImpl : TutorialPresentation {
        override val message: MutableStateFlow<(@Composable () -> Unit)?> = MutableStateFlow(null)
        override val buttonName = MutableStateFlow(DEFAULT_BUTTON_NAME)
        override val tooltip: MutableStateFlow<(@Composable RowScope.() -> Unit)?> = MutableStateFlow(null)
    }

    inner class TutorialRequestsImpl : TutorialRequests() {
        private val lock = Mutex()
        override val request: MutableStateFlow<TutorialRequest<*>?> = MutableStateFlow(null)

        suspend fun <R> issueAndAwait(request: TutorialRequest<R>): R = lock.withLock {
            this.request.value = request
            try {
                return request.awaitResponse()
            } finally {
                this.request.value = null
            }
        }
    }

    inner class StepSessionImpl(
        override val index: Int,
        override val step: Step,
    ) : StepSession {
        private val stepScope = this@TutorialSessionImpl.tutorialScope.childSupervisorScope()

        override val state: MutableStateFlow<StepState> = MutableStateFlow(StepState.NotInvoked)
        private val revokers: Revokers = Revokers()

        @Volatile
        private var currentInvocation: Job? = null

        private val executionContext = object : StepActionContext {
            private val lock = Mutex()
            override suspend fun delay(duration: Duration) {
                kotlinx.coroutines.delay(duration)
            }

            override suspend fun showPossibleMoves(pos: BoardPos, duration: Duration) {
                val req = TutorialRequest.ShowPossibleMoves(pos, duration)
                requests.issueAndAwait(req)
            }

            override suspend fun movePlayer(from: BoardPos, to: BoardPos): Unit = lock.withLock {
                check(
                    game.currentRound.first().move(from, to)
                ) { "Step '${step.name}': Failed to move player from $from to $to" }
                logger.info { "Step '${step.name}': Moved player from $from to $to" }
                revokers.add("move $to to $from") { game.currentRound.first().revertLast() }
            }

            override suspend fun moveOpponent(from: BoardPos, to: BoardPos): Unit = lock.withLock {
                check(
                    game.currentRound.first().move(from, to)
                ) { "Step '${step.name}': Failed to move opponent from $from to $to" }
                logger.info { "Step '${step.name}': Moved opponent from $from to $to" }
                revokers.add("move $to to $from") { game.currentRound.first().revertLast() }
            }

            override suspend fun requestMovePlayer(from: BoardPos, to: BoardPos) = lock.withLock {
                val playerMove = requests.issueAndAwait(TutorialRequest.MovePlayer(from, to))
                // TODO: check if move is correct, otherwise request again
            }

            override suspend fun flashKeizarPiece() {
                requests.issueAndAwait(TutorialRequest.FlashKeizar())
            }

            override suspend fun tooltip(duration: Duration, content: @Composable() (RowScope.() -> Unit)) {
                presentation.tooltip.value = content
                if (duration == Duration.INFINITE) {
                    // Ensure that tooltip is always removed when revoked
                    revokers.add("remove tooltip") { presentation.tooltip.value = null }
                } else {
                    try {
                        kotlinx.coroutines.delay(duration)
                    } finally { // covers cancellation
                        presentation.tooltip.value = null
                    }
                }
            }

            override suspend fun removeTooltip() {
                presentation.tooltip.value = null
            }

            override suspend fun message(content: @Composable () -> Unit) = lock.withLock {
                presentation.message.value = content
                logger.info { "Step '${step.name}': Displayed message" }
            }

            override suspend fun compose(content: @Composable (request: TutorialRequest.CompletableTutorialRequest<Unit>) -> Unit) {
                requests.issueAndAwait(TutorialRequest.Compose(content))
                logger.info { "Step '${step.name}': Displayed composable" }
            }

            override suspend fun awaitNext(buttonName: String) {
                presentation.buttonName.value = buttonName
                try {
                    requests.issueAndAwait(TutorialRequest.ClickNext())
                } finally {
                    presentation.buttonName.value = DEFAULT_BUTTON_NAME
                }
            }
        }

        fun skip() {
            logger.info { "Skipping step '${step.name}'" }
            val step = step
            check(state.compareAndSet(expect = StepState.NotInvoked, update = StepState.NothingInvoked)) {
                "Internal error: attempting to invoke step '${step.name}' that is currently in the ${state.value} state."
            }
        }

        /**
         * Launches a job to execute the step action.
         *
         * The job is stored in [currentLoopJob].
         *
         * It is guaranteed that when the job completes, the state of the step will be updated to any of the INVOKED states, i.e. [StepState.FullyInvoked] or [StepState.PartiallyInvoked].
         */
        suspend fun invoke() {
            logger.info { "Step '${step.name}' start" }

            val step = step
            check(state.compareAndSet(expect = StepState.NotInvoked, update = StepState.Invoking)) {
                "Internal error: attempting to invoke step '${step.name}' that is currently in the ${state.value} state."
            }

            val job = stepScope.launch {
                try {
                    when (step) {
                        is Action -> step.action(executionContext)
                        is Savepoint -> step.action(executionContext)
                        else -> {}
                    }
                } catch (e: CancellationException) {
                    // revoked
                    state.value = StepState.PartiallyInvoked
                    logger.info { "Step '${step.name}' cancelled" }
                    throw e
                } catch (e: Throwable) {
                    logger.info { "Step '${step.name}' exception" }
                    throw IllegalStateException("Exception in invoking step '${step.name}', see cause for details", e)
                }
                logger.info { "Step '${step.name}' succeed" }

                // normally succeed

                check(state.value == StepState.Invoking) {
                    "Step '${step.name}' check failed: ${state.value} != StepState.INVOKING"
                }
                state.value = StepState.FullyInvoked
            }
            currentInvocation = job
            job.join() // does not throw on CancellationException
        }

        suspend fun revoke() {
            logger.info { "Revoking step '${step.name}' (index=$index)" }
            // CAS update state to REVOKING
            while (true) {
                val value = state.value
                if (value !is StepState.Invoked && value !is StepState.Invoking) {
                    error("Internal error: attempting to revoke step '${step.name}' that is currently in the ${state.value} state.")
                }
                if (state.compareAndSet(expect = value, update = StepState.Revoking)) {
                    break
                } // else: lost race, try again
            }

            // Cancel existing invocation
            currentInvocation?.run {
                cancel(CancellationException("Revoking step '${step.name}'"))
                join()
            }

            val stateBeforeRevokers = state.value
            check(!stateBeforeRevokers.isJobRunning) {
                "Step ${step.name} check failed: $stateBeforeRevokers != StepState.REVOKING"
            }

            for (revoker in revokers.revokers) {
                try {
                    logger.info { "Revoking step '${step.name}': run revoker '${revoker.name}'" }
                    revoker.action()
                } catch (e: Throwable) {
                    throw IllegalStateException(
                        "Exception in revoking step '${step.name}', revoker is '${revoker.name}', see cause for details",
                        e
                    )
                }
            }
            revokers.revokers.clear()

            check(state.value == stateBeforeRevokers) {
                "Step ${step.name} check failed: concurrent modification: was ${stateBeforeRevokers}, now ${state.value}"
            }
            state.value = StepState.NotInvoked
        }

        override fun toString(): String {
            return "StepSessionImpl(step=${step.name}, state=${state.value})"
        }
    }
}
