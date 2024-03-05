package org.keizar.android.ui.foundation

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager


fun Modifier.moveFocusOnEnter(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    onPreviewKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown && (keyEvent.key == Key.Tab || keyEvent.key == Key.Enter)) {
            true // Event consumed
        } else if (keyEvent.type == KeyEventType.KeyUp && (keyEvent.key == Key.Tab || keyEvent.key == Key.Enter)) {
            focusManager.moveFocus(FocusDirection.Down)
            true // Event consumed
        } else {
            false // Event not consumed
        }
    }
}
