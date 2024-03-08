package org.keizar.android.ui.game.actions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.keizar.android.ui.game.GameBoardViewModel
import org.keizar.android.ui.game.rememberSinglePlayerGameBoardForPreview

@Composable
fun UndoButton(vm: GameBoardViewModel) {
    val canUndo by vm.canUndo.collectAsState()
    val winner by vm.winner.collectAsState()

    if (winner == null) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { vm.undo() },
                enabled = canUndo,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canUndo) MaterialTheme.colorScheme.primary else Color.LightGray,
                    contentColor = if (canUndo) MaterialTheme.colorScheme.onPrimary else Color.Gray
                )
            ) {
                Text("Undo")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewUndoButton() {
    UndoButton(
        rememberSinglePlayerGameBoardForPreview()
    )
}
