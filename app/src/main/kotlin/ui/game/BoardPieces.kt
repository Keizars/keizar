package org.keizar.android.ui.game

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import org.keizar.game.Role
import org.keizar.game.TileType
import kotlin.time.Duration.Companion.seconds


/**
 * Composes all the pieces on the board.
 *
 * This composable takes the maximum size available using [BoxWithConstraints] and arranges the pieces accordingly to fill that size.
 *
 * Pieces do not layout in a centered manner if `maxWidth` and `maxHeight` is not a square.
 * Therefore, you would like to place [BoardPieces] in a square container in order to see the correct piece layout.
 *
 * @see BoardTiles
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

        val tileSize by pieceArranger.tileSize.collectAsStateWithLifecycle(DpSize.Zero)

        val pieces by vm.pieces.collectAsStateWithLifecycle(emptyList())
        for (piece in pieces) {
            val isVisible by piece.isVisible.collectAsStateWithLifecycle(false)
            if (!isVisible) {
                continue
            }

            val targetOffset by piece.offsetInBoard.collectAsStateWithLifecycle(DpOffset.Zero)

            val offsetX by animateDpAsState(
                targetValue = targetOffset.x,
                animationSpec = vm.boardTransitionController.pieceMovementAnimationSpec,
                label = "offsetX"
            )
            val offsetY by animateDpAsState(
                targetValue = targetOffset.y,
                animationSpec = vm.boardTransitionController.pieceMovementAnimationSpec,
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

            val pos by piece.pos.collectAsStateWithLifecycle()
            val isCaptured by piece.isCaptured.collectAsStateWithLifecycle()

            Box(
                Modifier
                    .background(Color.Transparent)
                    .offset(offsetX, offsetY)
                    .then(
                        if (vm.boardProperties.tileArrangement[pos] == TileType.KEIZAR && !isCaptured) {
                            Modifier.alpha(vm.boardTransitionController.winningPieceAlpha)
                        } else Modifier
                    )
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
                        indication = null,
                    ) { vm.onClickPiece(piece) }
                    .size(tileSize), // placement modifiers
                contentAlignment = Alignment.Center,
            ) {
                PieceIconFitted(tileSize, pick?.piece == piece, piece.role.pieceColor())
            }
        }
    }
}

/**
 * [PieceIcon] fitted into tile size.
 *
 * @param isPicked gives a shadow effect if true.
 */
@Composable
fun PieceIconFitted(
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
        PieceIcon(color = color, Modifier.matchParentSize())
    }
}

/**
 * Returns the color of the piece according to the role.
 */
@Stable
fun Role.pieceColor() =
    if (this == Role.BLACK) Color.Black else Color.White

/**
 * A filled circle with a border and shadow, representing the piece.
 *
 * It's size is zero by default. You should specify the size using the [modifier].
 */
@Composable
internal fun PieceIcon(
    color: Color,
    modifier: Modifier,
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
        val vm = rememberSinglePlayerGameBoardForPreview()
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

@Preview
@Composable
private fun PreviewFlashKeizar() {
    val vm = rememberSinglePlayerGameBoardForPreview()
    Box(modifier = Modifier.alpha(vm.boardTransitionController.winningPieceAlpha)) {
        PieceIconFitted(
            tileSize = DpSize(320.dp, 320.dp),
            isPicked = false,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    LaunchedEffect(true) {
        while (true) {
            println("set")
            vm.boardTransitionController.flashWiningPiece()
            println("done")
            delay(3.seconds)
        }
    }
}