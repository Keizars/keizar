package org.keizar.utils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun CoroutineScope.childSupervisorScope(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineScope {
    return CoroutineScope(this.coroutineContext + coroutineContext + SupervisorJob(coroutineContext[Job]))
}

fun CoroutineScope.childScope(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineScope {
    return CoroutineScope(this.coroutineContext + coroutineContext + Job(coroutineContext[Job]))
}

fun CoroutineContext.childSupervisorScope(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineScope {
    return CoroutineScope(this + coroutineContext + SupervisorJob(coroutineContext[Job]))
}

fun CoroutineContext.childScope(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineScope {
    return CoroutineScope(this + coroutineContext + Job(coroutineContext[Job]))
}
