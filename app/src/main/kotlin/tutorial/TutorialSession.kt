package org.keizar.android.tutorial

import androidx.compose.runtime.Composable
import kotlinx.atomicfu.atomic
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

    private val stepStates = tutorial.steps.map { step ->
        StepSessionImpl(step = step)
    }

    override val currentStep: MutableStateFlow<StepSessionImpl> = MutableStateFlow(stepStates.first())
    override val requests = TutorialRequestsImpl()
    override val presentation = TutorialPresentationImpl()

    private val executionLock = Mutex()
    private var currentExecution: Job? = null

    private val logger = logger("TutorialSessionImpl")

    override suspend fun back() = executionLock.withLock {
        check(!started.value) { "Cannot revoke steps when the tutorial has not yet started" }

        val currentExecution = currentExecution
        check(currentExecution != null)

        // Cancel execution
        currentExecution.cancel(CancellationException("Revoking the current step"))
        currentExecution.join()
        this.currentExecution = null

        // Revoke current step
        val currentStep = currentStep.value
        currentStep.revoke()

        // Revoke previous step if it exists
        val currentStepIndex = stepStates.indexOf(currentStep)
        if (currentStepIndex > 0) {
            val previousStep = stepStates[currentStepIndex - 1]
            previousStep.revoke()
            this.currentStep.value = previousStep
        } else {
            // No previous step, do nothing
        }

        // Start again
        launchSequentialExecution()
    }

    override suspend fun start() = executionLock.withLock {
        if (!started.compareAndSet(expect = false, update = true)) {
            return
        }

        launchSequentialExecution()
    }

    private fun launchSequentialExecution() {
        check(currentExecution == null)
        currentExecution = tutorialScope.launch {
            for (step in stepStates) {
                logger.info { "Executing step '${step.step.name}'" }
                currentStep.value = step
                step.invoke()
            }
        }
    }

    inner class TutorialPresentationImpl : TutorialPresentation {
        override val message: MutableStateFlow<(@Composable () -> Unit)?> = MutableStateFlow(null)
    }

    inner class TutorialRequestsImpl : TutorialRequests() {
        private val lock = Mutex()
        override val request: MutableStateFlow<TutorialRequest<*>?> = MutableStateFlow(null)

        suspend fun <R> issueAndAwait(request: TutorialRequest<R>): R = lock.withLock {
            this.request.value = request
            return request.awaitResponse().also {
                this.request.value = null
            }
        }
    }

    inner class StepSessionImpl(
        override val step: Step,
    ) : StepSession {
        private val stepScope = this@TutorialSessionImpl.tutorialScope.childSupervisorScope()

        override val state: MutableStateFlow<StepState> = MutableStateFlow(StepState.NOT_INVOKED)
        private val revokers: MutableList<suspend () -> Unit> = mutableListOf()

        @Volatile
        private var currentInvocation: Job? = null

        private val executionContext = object : StepActionContext {
            private val lock = Mutex()
            override suspend fun delay(duration: Duration) {
                kotlinx.coroutines.delay(duration)
            }

            override suspend fun movePlayer(from: BoardPos, to: BoardPos): Unit = lock.withLock {
                val myRole = game.currentRound.first().curRole.first()
                check(
                    game.currentRound.first().move(from, to)
                ) { "Step '${step.name}': Failed to move player from $from to $to" }
                revokers.add { game.currentRound.first().undo(myRole) }
            }

            override suspend fun moveOpponent(from: BoardPos, to: BoardPos): Unit = lock.withLock {
                val myRole = game.currentRound.first().curRole.first()
                check(
                    game.currentRound.first().move(from, to)
                ) { "Step '${step.name}': Failed to move opponent from $from to $to" }
                revokers.add { game.currentRound.first().undo(myRole) }
            }

            override suspend fun requestMovePlayer(from: BoardPos, to: BoardPos) = lock.withLock {
                val playerMove = requests.issueAndAwait(TutorialRequest.MovePlayer(from, to))
                // TODO: check if move is correct, otherwise request again
            }

            override suspend fun message(content: @Composable () -> Unit) = lock.withLock {
                presentation.message.value = content
            }

            override suspend fun awaitNext() {
                requests.issueAndAwait(TutorialRequest.ClickNext())
            }
        }

        suspend fun invoke() {
            check(state.compareAndSet(expect = StepState.NOT_INVOKED, update = StepState.INVOKING)) {
                "Internal error: attempting to invoke step '${step.name}' that is currently in the ${state.value} state."
            }

            currentInvocation = stepScope.launch {
                step.action(executionContext)
            }

            try {
                step.action(executionContext)
            } catch (e: Throwable) {
                throw IllegalStateException("Exception in invoking step '${step.name}', see cause for details", e)
            }

            check(state.value == StepState.INVOKING)
            state.value = StepState.INVOKED
        }

        suspend fun revoke() {
            logger.info { "Revoking step '${step.name}'" }
            // CAS update state to REVOKING
            while (true) {
                val value = state.value
                if (!(value == StepState.INVOKING || value == StepState.INVOKED)) {
                    error("Internal error: attempting to revoke step '${step.name}' that is currently in the ${state.value} state.")
                }
                if (state.compareAndSet(expect = value, update = StepState.REVOKING)) {
                    break
                } // else: lost race, try again
            }

            // Cancel existing invocation
            currentInvocation?.run {
                cancel(CancellationException("Revoking step '${step.name}'"))
                join()
            }

            for (revoker in revokers) {
                try {
                    revoker()
                } catch (e: Throwable) {
                    throw IllegalStateException("Exception in revoking step '${step.name}', see cause for details", e)
                }
            }

            check(state.value == StepState.REVOKING)
            state.value = StepState.NOT_INVOKED
        }

        override fun toString(): String {
            return "StepSessionImpl(step=${step.name}, state=${state.value})"
        }
    }
}
