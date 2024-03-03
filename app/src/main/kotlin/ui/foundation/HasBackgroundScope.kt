package org.keizar.android.ui.foundation

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

class BackgroundScope(
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : HasBackgroundScope {
    override val backgroundScope: CoroutineScope =
        CoroutineScope(coroutineContext + SupervisorJob(coroutineContext[Job]))
}

interface HasBackgroundScope {
    val backgroundScope: CoroutineScope

    fun <T> Flow<T>.shareInBackground(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
        replay: Int = 1,
    ): SharedFlow<T> = shareIn(backgroundScope, started, replay)

    fun <T> Flow<T>.stateInBackground(
        initialValue: T,
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
    ): StateFlow<T> = stateIn(backgroundScope, started, initialValue)

    fun <T> Flow<T>.stateInBackground(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
    ): StateFlow<T?> = stateIn(backgroundScope, started, null)


    fun <T> Flow<T>.runningList(): Flow<List<T>> {
        return runningFold(emptyList()) { acc, value ->
            acc + value
        }
    }

    fun <T> deferFlowInBackground(value: suspend () -> T): MutableStateFlow<T?> {
        val flow = MutableStateFlow<T?>(null)
        launchInBackground {
            flow.value = value()
        }
        return flow
    }

    fun <T, R> Flow<T>.mapLatestSupervised(transform: suspend CoroutineScope.(value: T) -> R): Flow<R> =
        mapLatest {
            supervisorScope { transform(it) }
        }

    fun <T> Flow<T>.localCachedStateFlow(initialValue: T): MutableStateFlow<T> {
        val localFlow = MutableStateFlow(initialValue)
        val mergedFlow: StateFlow<T> = merge(this, localFlow).stateInBackground(initialValue)
        return object : MutableStateFlow<T> by localFlow {
            override var value: T
                get() = mergedFlow.value
                set(value) {
                    localFlow.value = value
                }

            override val replayCache: List<T> get() = mergedFlow.replayCache

            override suspend fun collect(collector: FlowCollector<T>): Nothing {
                mergedFlow.collect(collector)
            }
        }
    }

    fun <T> Flow<T>.localCachedSharedFlow(
        started: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
        replay: Int = 1,
        onBufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
    ): MutableSharedFlow<T> {
        val localFlow = MutableSharedFlow<T>(replay, onBufferOverflow = onBufferOverflow)
        val mergedFlow: SharedFlow<T> = merge(this, localFlow).shareInBackground(started, replay = replay)
        return object : MutableSharedFlow<T> by localFlow {
            override val replayCache: List<T> get() = mergedFlow.replayCache

            override suspend fun collect(collector: FlowCollector<T>): Nothing {
                mergedFlow.collect(collector)
            }
        }
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

fun <V : HasBackgroundScope> V.launchInBackgroundAnimated(
    isLoadingState: MutableState<Boolean>,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend V.() -> Unit,
): Job {
    isLoadingState.value = true
    return backgroundScope.launch(context, start) {
        block()
        isLoadingState.value = false
    }
}


fun <T> CoroutineScope.deferFlow(value: suspend () -> T): MutableStateFlow<T?> {
    val flow = MutableStateFlow<T?>(null)
    launch {
        flow.value = value()
    }
    return flow
}
