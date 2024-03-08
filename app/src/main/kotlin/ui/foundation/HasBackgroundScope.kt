package org.keizar.android.ui.foundation

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.ktor.util.logging.error
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * Creates a new background scope.
 *
 * Note that this functions it not intended to be used in-place.
 * Doing `BackgroundScope().backgroundScope.launch { }` is an error - it effectively leaks the coroutine into an unmanaged scope.
 *
 * @param coroutineContext parent coroutine context to pass in the background scope.
 * If the parent context has a [Job], the scope will use it as a parent job.
 *
 * @see HasBackgroundScope
 */
@Suppress("FunctionName")
fun BackgroundScope(
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): HasBackgroundScope = SimpleBackgroundScope(coroutineContext)

private class SimpleBackgroundScope(
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : HasBackgroundScope {
    override val backgroundScope: CoroutineScope =
        CoroutineScope(coroutineContext + SupervisorJob(coroutineContext[Job]))
}

/**
 * A scope that provides a background scope for launching background jobs.
 *
 * [HasBackgroundScope] also provides various helper functions for flows.
 *
 * ## Creating a background scope
 *
 * It is recommended to use the constructor-like function [BackgroundScope] to create a background scope.
 *
 * A special use case is [AbstractViewModel], which implements the [HasBackgroundScope] interface manually
 * to comply with Android lifecycle management.
 *
 * ## Example Usage
 *
 * A recommended usage is to use it in globally maintained class that implements [HasBackgroundScope]:
 * ```
 * class SessionManagerImpl : HasBackgroundScope by BackgroundScope() {
 * }
 * ```
 *
 * SessionManager is a singleton, and injected into other objects.
 * A background scope can be beneficial for the SessionManager implementation to launch background jobs.
 *
 * ## Hiding BackgroundScope in public API
 *
 * It is recommended to only use [HasBackgroundScope] in internal implementations,
 * so that public users of your API does not see the background scope and can't misuse it - launching a job in a scope that they don't control is bad.
 */
interface HasBackgroundScope {
    /**
     * The background scope for launching background jobs.
     *
     * It must have a [SupervisorJob], to control structural concurrency.
     * A [CoroutineExceptionHandler] is also installed to prevent app crashing.
     */
    val backgroundScope: CoroutineScope

    /**
     * Converts a _cold_ [Flow] into a _hot_ [SharedFlow] that is started in the **background scope**.
     *
     * ## No UI actions in flow operations
     *
     * Since the flow is started in the background scope, you must not perform any UI actions in the flow operations.
     * All UI actions will fail with an exception.
     *
     * ## Lazy Sharing
     *
     * By default, sharing is started **only when** the first subscriber appears, immediately stops when the last
     * subscriber disappears (by default), keeping the replay cache forever (by default).
     *
     * If there is no subscriber, the flow will not be collected. As such, the returned flow does not immediately have a value.
     *
     * @see Flow.shareIn
     */
    fun <T> Flow<T>.shareInBackground(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
        replay: Int = 1,
    ): SharedFlow<T> = shareIn(backgroundScope, started, replay)

    /**
     * Converts a _cold_ [Flow] into a _hot_ [StateFlow] that is started in the background scope.
     *
     * ## No UI actions in flow operations
     *
     * Since the flow is started in the background scope, you must not perform any UI actions in the flow operations.
     * All UI actions will fail with an exception.
     *
     * ## Lazy Sharing
     *
     * By default, sharing is started **only when** the first subscriber appears, immediately stops when the last
     * subscriber disappears (by default), keeping the replay cache forever (by default).
     *
     * If there is no subscriber, the flow will not be collected. As such,
     * the [StateFlow.value] of the returned [StateFlow] will keeps being [initialValue], unless there is a subscriber.
     *
     * ## `StateFlow.first` is not a subscriber
     *
     * Calling `StateFlow.first` is not considered a subscriber. So you will always get the `initialValue` when calling `first`,
     * unless the flow is being collected.
     *
     * @see Flow.stateIn
     */
    fun <T> Flow<T>.stateInBackground(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
    ): StateFlow<T> = stateIn(backgroundScope, started, initialValue)

    /**
     * Converts a _cold_ [Flow] into a _hot_ [StateFlow] that is started in the **background scope**.
     *
     * The returned [StateFlow] initially has a `null` [StateFlow.value].
     *
     * ## No UI actions in flow operations
     *
     * Since the flow is started in the background scope, you must not perform any UI actions in the flow operations.
     * All UI actions will fail with an exception.
     *
     * ## Lazy Sharing
     *
     * By default, sharing is started **only when** the first subscriber appears, immediately stops when the last
     * subscriber disappears (by default), keeping the replay cache forever (by default).
     *
     * If there is no subscriber, the flow will not be collected. As such,
     * the [StateFlow.value] of the returned [StateFlow] will keeps being [initialValue], unless there is a subscriber.
     *
     * ## `StateFlow.first` is not a subscriber
     *
     * Calling `StateFlow.first` is not considered a subscriber. So you will always get the `initialValue` when calling `first`,
     * unless the flow is being collected.
     *
     * @see Flow.stateIn
     */
    fun <T> Flow<T>.stateInBackground(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
    ): StateFlow<T?> = stateIn(backgroundScope, started, null)

    /**
     * Converts a _cold_ [Flow] into a _hot_ [StateFlow] that is started in the **background scope**,
     * and merge it with a mutable local cache.
     *
     * The returned flow is a [MutableStateFlow], which acts like a local cache and allows you to update it.
     * When [this] emits a new value, the resulting flow is updated with the new value.
     *
     * ## No UI actions in flow operations
     *
     * Since the flow is started in the background scope, you must not perform any UI actions in the flow operations.
     * All UI actions will fail with an exception.
     *
     * ## Example usage
     * ```
     * val allGames: MutableStateFlow<List<GameDataGet>> = time.map {
     *     gameDataService.getGames()
     * }.localCachedStateFlow(emptyList())
     * ```
     *
     * The example example converts the mapped flow to a locally cached state flow.
     * When `time` changes, `gameDataService.getGames()` executes and the flow emits a new list of games fetched from the server.
     *
     * When the user deletes a game, the local cache can be updated with the new list of games without the deleted game,
     * so that we do not need to prefetch the entire list of games from the server.
     *
     * ```
     * suspend fun deleteGame(id: String) {
     *     runCatching {
     *         gameDataService.deleteGame(id) // Send a request to the server to delete the game
     *     }.onSuccess {
     *         // When the request is successful, update the local cache to remove the deleted game
     *         allGames.value = allGames.value.filter { it.id != id }
     *     }
     * }
     * ```
     */
    fun <T> Flow<T>.localCachedStateFlow(
        initialValue: T,
//        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
    ): MutableStateFlow<T> {
        val localFlow = MutableStateFlow(initialValue)
        launchInBackground {
            collect { localFlow.value = it }
        }
        return localFlow
    }
}


/**
 * Launches a new coroutine job in the background scope.
 *
 * Note that UI jobs are not allowed in this scope. To launch a UI job, use [launchInMain].
 */
fun <V : HasBackgroundScope> V.launchInBackground(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    @WorkerThread block: suspend V.() -> Unit,
): Job {
    return backgroundScope.launch(start = start) {
        block()
    }
}


/**
 * Launches a new coroutine job in the background scope.
 *
 * Note that UI jobs are not allowed in this scope. To launch a UI job, use [launchInMain].
 */
fun <V : HasBackgroundScope> V.launchInBackground(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    @WorkerThread block: suspend V.() -> Unit,
): Job {
    return backgroundScope.launch(context, start) {
        block()
    }
}

/**
 * Launches a new coroutine job in the UI scope.
 *
 * Note that you must not perform any costly operations in this scope, as this will block the UI.
 * To perform costly computation, use [launchInBackground].
 */
fun <V : HasBackgroundScope> V.launchInMain(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    @UiThread block: suspend V.() -> Unit,
): Job {
    return backgroundScope.launch(context + Dispatchers.Main, start) {
        block()
    }
}

/**
 * Runs the block multiple times, returns when it succeeds the first time. with a delay between each attempt.
 */
suspend inline fun <R, V : HasBackgroundScope> V.runUntilSuccess(block: V.() -> R): R {
    contract { callsInPlace(block, InvocationKind.AT_LEAST_ONCE) }
    while (currentCoroutineContext().isActive) {
        try {
            return block()
        } catch (e: Exception) {
            if (this is AbstractViewModel) {
                logger.error(e)
            } else {
                e.printStackTrace()
            }
            delay(3.seconds)
        }
    }
    yield() // throws CancellationException()
    throw CancellationException() // should not reach, defensive
}