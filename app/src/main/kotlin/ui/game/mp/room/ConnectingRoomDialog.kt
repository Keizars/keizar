package org.keizar.android.ui.game.mp.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview


@Composable
fun ConnectingRoomDialog(
    text: @Composable () -> Unit = { Text("Connecting...") },
    extra: @Composable ColumnScope.() -> Unit = {},
    progress: @Composable (RowScope.() -> Unit)? = {
        LinearProgressIndicator(
            Modifier.width(128.dp)
        )
    },
    confirmButton: @Composable (() -> Unit)? = null,
    onDismissRequest: () -> Unit = {},
    properties: DialogProperties = DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false,
    ),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            Modifier
                .width(IntrinsicSize.Min)
                .clip(shape)
                .shadow(4.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(modifier = Modifier.width(IntrinsicSize.Max)) {
                ProvideTextStyle(value = MaterialTheme.typography.titleMedium) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                        text()
                    }
                }
            }
            progress?.let { p ->
                Row(modifier = Modifier.padding(top = 32.dp)) {
                    p()
                }
            }
            extra()

            if (confirmButton != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    confirmButton()
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewConnectingRoomDialog() {
    ProvideCompositionalLocalsForPreview {
        ConnectingRoomDialog()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewConnectingRoomDialogConfirm() {
    ProvideCompositionalLocalsForPreview {
        ConnectingRoomDialog(confirmButton = {
            TextButton(onClick = { }) {
                Text("Cancel")
            }
        })
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewConnectingRoomDialogConnecting() {
    ProvideCompositionalLocalsForPreview {
        ConnectingRoomDialog(
            text = { Text(text = "Connection lost, reconnecting...") },
            confirmButton = {
                TextButton(onClick = { }) {
                    Text("Cancel")
                }
            }
        )
    }
}
