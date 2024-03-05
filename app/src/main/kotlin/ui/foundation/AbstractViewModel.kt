package org.keizar.android.ui.foundation

import androidx.annotation.CallSuper
import androidx.compose.runtime.RememberObserver
import androidx.lifecycle.ViewModel
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.logger
import me.him188.ani.utils.logging.trace

interface Disposable {
    fun dispose()
}

/**
 * A view model that provides a background scope and automatically disposes of it when it is no longer needed.
 *
 * All view models should extend this class.
 *
 * ## Lifecycle Management
 *
 * [AbstractViewModel] is a [RememberObserver], which means it can and must be remembered and forgotten by Compose.
 * When it is remembered, it will create a background scope and call [init]. When it is forgotten, it will dispose of the background scope.
 *
 * A typical usage is to remember the view model in a composable, so that Compose can automatically manage the lifecycle.
 */
abstract class AbstractViewModel : RememberObserver, ViewModel(), HasBackgroundScope, Disposable {
    protected val logger by lazy { logger(this::class) }

    private val closed = atomic(false)
    private val isClosed get() = closed.value

    private var _backgroundScope = createBackgroundScope()
    final override val backgroundScope: CoroutineScope
        get() {
            return _backgroundScope
        }


    @CallSuper
    final override fun onAbandoned() {
        logger.trace { "${this::class.simpleName} onAbandoned" }
        dispose()
    }

    /**
     * Disposes this view model, cancelling the background scope.
     *
     * If you override this method, you must call `super.dispose()`.
     */
    @CallSuper
    override fun dispose() {
        if (!closed.compareAndSet(expect = false, update = true)) {
            return
        }
//        if (_backgroundScope.isInitialized()) {
        backgroundScope.cancel()
//        }
    }

    private var referenceCount = 0

    @CallSuper
    final override fun onForgotten() {
        referenceCount--
        logger.trace { "${this::class.simpleName} onForgotten, remaining refCount=$referenceCount" }
        if (referenceCount == 0) {
            dispose()
        }
    }

    @CallSuper
    final override fun onRemembered() {
        referenceCount++
        logger.trace { "${this::class.simpleName} onRemembered, refCount=$referenceCount" }
        if (!_backgroundScope.isActive) {
            _backgroundScope = createBackgroundScope()
        }
        if (referenceCount == 1) {
            this.init() // first remember
        }
    }

    private fun createBackgroundScope(): CoroutineScope {
        return CoroutineScope(CoroutineExceptionHandler { _, throwable ->
            logger.error(throwable) { "Unhandled exception in background scope" }
        } + SupervisorJob())
    }

    /**
     * Called when the view model is remembered.
     */
    protected open fun init() {
    }
}
