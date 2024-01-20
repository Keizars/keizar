package org.keizar.android.ui.game

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.keizar.game.BoardProperties
import org.keizar.game.Player
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds


/**
 * Composes pieces on the board.
 *
 * @see BoardBackground
 */
@Composable
fun BoardPieces(
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        // Notify the PieceArranger with updated dimensions
        val pieceArranger = vm.pieceArranger
        SideEffect {
            pieceArranger.setDimensions(maxWidth, maxHeight)
        }

        // Animate position after initialization
        var loaded by remember { mutableStateOf(false) }
        LaunchedEffect(true) {
            delay(1.seconds)
            loaded = true
        }

        val tileSize by pieceArranger.tileSize.collectAsStateWithLifecycle(DpSize.Zero)

        for (piece in vm.pieces) {
            val isCaptured by piece.isCaptured.collectAsStateWithLifecycle()
            if (isCaptured) {
                continue
            }
            val targetOffset by piece.offsetInBoard.collectAsStateWithLifecycle(DpOffset.Zero)
            val offsetX by animateDpAsState(
                targetValue = targetOffset.x,
                animationSpec = if (loaded) tween() else snap(),
                label = "offsetX"
            )
            val offsetY by animateDpAsState(
                targetValue = targetOffset.y,
                animationSpec = if (loaded) tween() else snap(),
                label = "offsetY"
            )
            Box(
                Modifier
                    .background(Color.Transparent)
                    .offset(offsetX, offsetY)
                    .clickable(
                        remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) { vm.onClickPiece(piece) }
                    .clip(CircleShape)
                    .size(tileSize)
                    .padding(10.dp),
            ) {
                val color = piece.player.pieceColor()
                PlayerIcon(color = color, Modifier.matchParentSize())
            }
        }
    }
}

@Stable
fun Player.pieceColor() =
    if (this == Player.BLACK) Color.Black else Color.White

@Composable
internal fun PlayerIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .border(0.5.dp, Color.Gray, shape = CircleShape)
            .padding(all = 4.5.dp)
            .shadow(1.dp, CircleShape)
            .clip(CircleShape)
            .border(1.dp, Color.Gray, shape = CircleShape)
            .shadow(1.dp, CircleShape),
    ) {
        Spacer(
            Modifier
                .matchParentSize()
                .background(color),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBoardPiecesWithBackground() {
    BoxWithConstraints {
        val prop = remember {
            BoardProperties.getStandardProperties(Random(0))
        }
        val vm = rememberGameBoardViewModel(prop)
        BoardBackground(
            prop,
            vm,
            Modifier.size(min(maxWidth, maxHeight))
        )
        BoardPieces(vm = vm)
    }
}