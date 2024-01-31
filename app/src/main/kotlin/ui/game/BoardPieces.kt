package org.keizar.android.ui.game

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.Player
import org.keizar.game.Role
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
            delay(2.seconds)
            loaded = true
        }

        val tileSize by pieceArranger.tileSize.collectAsStateWithLifecycle(DpSize.Zero)

        val pieces by vm.pieces.collectAsStateWithLifecycle()
        for (piece in pieces) {
            val isVisible by piece.isVisible.collectAsStateWithLifecycle(false)
            if (!isVisible) {
                continue
            }

            val targetOffset by piece.offsetInBoard.collectAsStateWithLifecycle(DpOffset.Zero)
            val lastMoveIsDrag by vm.lastMoveIsDrag.collectAsStateWithLifecycle(false)
            val shouldAnimateMovement = loaded && !lastMoveIsDrag

            val offsetX by animateDpAsState(
                targetValue = targetOffset.x,
                animationSpec = if (shouldAnimateMovement) tween() else snap(),
                label = "offsetX"
            )
            val offsetY by animateDpAsState(
                targetValue = targetOffset.y,
                animationSpec = if (shouldAnimateMovement) tween() else snap(),
                label = "offsetY"
            )

            val density = LocalDensity.current
            val draggableState = rememberDraggable2DState {
                with(density) {
                    vm.addDraggingOffset(DpOffset(it.x.toDp(), it.y.toDp()))
                }
            }

            val pick by vm.currentPick.collectAsStateWithLifecycle()
            val draggingOffset by vm.draggingOffset.collectAsStateWithLifecycle(DpOffset.Zero)

            Box(
                Modifier
                    .background(Color.Transparent)
                    .offset(offsetX, offsetY)
                    .then(
                        if (pick?.piece == piece) {
                            Modifier.absoluteOffset(draggingOffset.x, draggingOffset.y)
                        } else {
                            Modifier
                        }
                    )
                    .draggable2D(
                        draggableState,
                        onDragStarted = {
                            vm.onHold(piece)
                        },
                        onDragStopped = {
                            vm.onRelease(piece)
                        },
                    )
                    .clickable(
                        remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) { vm.onClickPiece(piece) }
                    .size(tileSize), // placement modifiers
                contentAlignment = Alignment.Center,
            ) {
                PlayerIconFitted(tileSize, pick?.piece == piece, piece.role.pieceColor())
            }
        }
    }
}

/**
 * Player icon fitted into tile size
 */
@Composable
fun PlayerIconFitted(
    tileSize: DpSize,
    isPicked: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier // size modifiers
            .size(tileSize * 0.45f)
            .clip(CircleShape)
            .then(
                // shadow if dragging
                if (isPicked) {
                    Modifier.shadow(4.dp, shape = CircleShape)
                } else {
                    Modifier
                }
            )
    ) {
        PlayerIcon(color = color, Modifier.matchParentSize())
    }
}

@Stable
fun Role.pieceColor() =
    if (this == Role.BLACK) Color.Black else Color.White

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
            BoardProperties.getStandardProperties(0)
        }
        val vm = rememberGameBoardViewModel(
            game = GameSession.create(prop),
            selfPlayer = Player.Player1,
        )
        BoardBackground(
            vm,
            Modifier.size(min(maxWidth, maxHeight))
        )
        BoardPieces(
            vm = vm,
            Modifier.size(min(maxWidth, maxHeight))
        )
    }
}