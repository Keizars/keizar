package org.keizar.android.ui.foundation

import android.widget.Toast
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import org.keizar.android.ui.game.mp.room.ConnectingRoomDialog
import kotlin.time.Duration.Companion.seconds

/**
 * A controller for a dialog that shows an error message and a cancel button.
 *
 * @see ErrorDialogHost
 */
interface ErrorDialogController {
    val isVisible: Boolean
        @Composable get

    fun hide()

    fun show()

    val debugInfo: String?
        @Composable get

    fun setDebugInfo(debugInfo: String?)
}

class StateErrorDialogController : ErrorDialogController {
    private var _isVisible: Boolean by mutableStateOf(false)

    override val isVisible: Boolean
        @Composable get() = _isVisible

    override fun hide() {
        _isVisible = false
    }

    override fun show() {
        _isVisible = true
    }

    private var _debugInfo: String? by mutableStateOf("")
    override val debugInfo: String?
        @Composable get() = _debugInfo


    override fun setDebugInfo(debugInfo: String?) {
        _debugInfo = debugInfo
    }
}

/**
 * Composes a [ErrorDialogHost] that shows a "Connection lost, reconnecting..." message and a cancel button.
 *
 * [errorFlow] is a flow of error messages that will trigger the dialog to show.
 * If the flow emits `null`, the dialog is hidden.
 *
 * @param onClickCancel the action to perform when the cancel button is clicked, when the error is [recovering][ErrorMessage.isRecovering].
 * @param onConfirm the action to perform when the confirm button is clicked, when the error is not [recovering][ErrorMessage.isRecovering].
 * @param clearError whether to set the [errorFlow] to `null` when "OK" is clicked
 */
@Composable
fun ErrorDialogHost(
    errorFlow: MutableStateFlow<ErrorMessage?>,
    onClickCancel: () -> Unit = {},
    onConfirm: () -> Unit = { },
    clearError: Boolean = false,
) {
    return ErrorDialogHost(
        errorFlow = errorFlow as Flow<ErrorMessage?>,
        onClickCancel = onClickCancel,
        onConfirm = {
            if (clearError) {
                errorFlow.value = null
            }
            onConfirm()
        },
    )
}

/**
 * Composes a [ErrorDialogHost] that shows a "Connection lost, reconnecting..." message and a cancel button.
 *
 * [errorFlow] is a flow of error messages that will trigger the dialog to show.
 * If the flow emits `null`, the dialog is hidden.
 *
 * @param onClickCancel the action to perform when the cancel button is clicked, when the error is [recovering][ErrorMessage.isRecovering].
 * @param onConfirm the action to perform when the confirm button is clicked, when the error is not [recovering][ErrorMessage.isRecovering].
 */
@Composable
fun ErrorDialogHost(
    errorFlow: Flow<ErrorMessage?>,
    onClickCancel: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    val controller = remember {
        StateErrorDialogController()
    }

    val error = remember(errorFlow) {
        errorFlow.distinctUntilChanged()
            .debounce(0.5.seconds)
    }.collectAsStateWithLifecycle(null).value

    LaunchedEffect(error) {
        if (error != null) {
            controller.show()
        } else {
            controller.hide()
        }
    }

    if (controller.isVisible) {
        ConnectingRoomDialog(
            text = {
                Text(text = error?.message ?: "Operation failed, please try again")
            },
            progress = if (error?.isRecovering == true) {
                {
                    LinearProgressIndicator(Modifier.width(128.dp))
                }
            } else null,
            onDismissRequest = {
                if (error?.isRecovering == false) {
                    controller.hide()
                }
            },
            confirmButton = {
                if (error?.isRecovering == false) {
                    TextButton(onClick = {
                        controller.hide()
                        onConfirm()
                    }) {
                        Text("OK")
                    }
                } else {
                    // recovering
                    TextButton(onClick = {
                        controller.hide()
                        onClickCancel()
                    }) {
                        Text("Cancel")
                    }
                }
            },
        )
    }
}

/**
 * Composes a [ReconnectingDialogHost] that shows a "Connection lost, reconnecting..." message and a cancel button
 * when [ErrorDialogController.isVisible] is `true`.
 */
@Composable
fun ReconnectingDialogHost(
    controller: ErrorDialogController,
    onClickCancel: () -> Unit = {},
) {
    if (controller.isVisible) {
        ConnectingRoomDialog(
            text = {
                Text(text = "Connection lost, reconnecting...")
            },
            confirmButton = {
                TextButton(onClick = {
                    controller.hide()
                    onClickCancel()
                }) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Shows a [Toast] with the error message when [error] emits an error message.
 */
@Composable
fun ErrorToast(
    error: Flow<ErrorMessage?>,
) {
    val context = LocalContext.current
    LaunchedEffect(error) {
        error.distinctUntilChanged()
            .debounce(0.5.seconds)
            .collect { error ->
                if (error != null) {
                    withContext(Dispatchers.Main.immediate) {
                        Toast.makeText(
                            context,
                            error.message ?: "Operation failed, please try again",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }
}

/**
 * An error message to be presented.
 */
interface ErrorMessage {
    val message: String?
    val cause: Throwable?

    val isRecovering: Boolean

    companion object {
        /**
         * A network error that is not automatically recovering.
         *
         * @param cause internal cause of the error, to be displayed when the app is built in debug mode
         */
        fun networkErrorRecovering(cause: Throwable? = null): ErrorMessage =
            SimpleErrorMessage("Network error, reconnecting...", cause, isRecovering = true)

        /**
         * A network error that is not automatically recovering.
         *
         * Users will see a simple alert dialog with the message and a "OK" button.
         *
         * @param cause internal cause of the error, to be displayed when the app is built in debug mode
         */
        fun networkError(cause: Throwable? = null): ErrorMessage =
            SimpleErrorMessage("Network error, please check your connection and try again", cause)

        fun simple(message: String?, cause: Throwable? = null): ErrorMessage =
            SimpleErrorMessage(message, cause)
    }

    private class SimpleErrorMessage(
        override val message: String?,
        override val cause: Throwable? = null,
        override val isRecovering: Boolean = false,
    ) : ErrorMessage
}
