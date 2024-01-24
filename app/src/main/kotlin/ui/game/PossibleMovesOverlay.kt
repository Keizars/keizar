package org.keizar.android.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.first
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.Player
import org.keizar.game.Role
import kotlin.random.Random

@Composable
fun PossibleMovesOverlay(
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        val availablePositions by vm.availablePositions.collectAsStateWithLifecycle(emptyList())

        availablePositions?.let { list ->
            for (boardPos in list) {
                val tileSize by vm.pieceArranger.tileSize.collectAsStateWithLifecycle(DpSize.Zero)
                val offset by remember(vm, boardPos) { vm.pieceArranger.offsetFor(boardPos) }
                    .collectAsStateWithLifecycle(DpOffset.Zero)

                val player by vm.selfRole.collectAsStateWithLifecycle()
                Box(
                    Modifier
                        .background(Color.Transparent)
                        .offset(offset.x, offset.y)
                        .alpha(0.3f)
                        .clip(CircleShape)
                        .size(tileSize)
                        .padding(10.dp),
                ) {
                    PlayerIcon(color = player.pieceColor(), Modifier.matchParentSize())
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPossibleMovesOverlay() {
    BoxWithConstraints {
        val vm = rememberGameBoardViewModel(
            game = GameSession.create(BoardProperties.getStandardProperties(0)),
            selfPlayer = Player.Player1,
        )
        LaunchedEffect(true) {
            vm.onClickPiece(vm.pieces.first().first { it.role == Role.WHITE })
        }
        PossibleMovesOverlay(
            vm,
            Modifier.size(min(maxWidth, maxHeight))
        )
    }
}