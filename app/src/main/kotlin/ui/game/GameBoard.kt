package org.keizar.android.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.min
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.keizar.game.BoardProperties
import kotlin.random.Random


@Composable
fun GameBoard(
    properties: BoardProperties,
    modifier: Modifier = Modifier,
) {
    val vm = rememberGameBoardViewModel(boardProperties = properties)
    Column(modifier = Modifier) {
        WinningCounter(vm)

        Box(modifier = modifier) {
            BoardBackground(properties, vm)
            BoardPieces(vm)
            PossibleMovesOverlay(vm)
        }

    }
}

@Composable
fun WinningCounter(vm: GameBoardViewModel) {
    val winningCounter by vm.winningCounter.collectAsState()
    Text(text = "Winning Keizar Counter: $winningCounter")
}


@Preview(showBackground = true)
@Composable
private fun PreviewGameBoard() {
    BoxWithConstraints {
        GameBoard(
            remember {
                BoardProperties.getStandardProperties(Random(0))
            },
            Modifier.size(min(maxWidth, maxHeight))
        )
    }
}