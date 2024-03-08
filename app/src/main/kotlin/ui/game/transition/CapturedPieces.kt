package org.keizar.android.ui.game.transition

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.keizar.android.ui.game.BoardBackground
import org.keizar.android.ui.game.BoardPieces
import org.keizar.android.ui.game.PossibleMovesOverlay
import org.keizar.android.ui.game.rememberSinglePlayerGameBoardViewModel
import org.keizar.game.snapshot.buildGameSession
import org.keizar.utils.communication.game.Player

/**
 * Represents a slot where a captured piece can be placed in.
 */
class CapturedPieceSlot {
    /**
     * The offset of the slot relative to the top-left corner of the board.
     */
    var offsetFromBoard: DpOffset by mutableStateOf(DpOffset.Zero)
        internal set

    /**
     * The index of the piece that is placed in this slot. `null` if the slot is empty.
     */
    var pieceIndex: Int? by mutableStateOf(null)
        internal set
}

/**
 * A state that manages the captured pieces.
 *
 * To create a new instance of [CapturedPieceHostState], use the [CapturedPieceHostState] factory function.
 *
 * @see [CapturedPiecesHost]
 */
interface CapturedPieceHostState {
    /**
     * UI Slots of the captured pieces where captured pieces can be placed in.
     *
     * Implementation is [SnapshotStateList].
     */
    @Stable
    val slots: List<CapturedPieceSlot>

    /**
     * Number of pieces currently captured.
     */
    val capturedPieceCount: Int
        @Composable
        get() = remember {
            derivedStateOf {
                slots.count { it.pieceIndex != null }
            }
        }.value

    /**
     * Attempts to capture the piece with the given [pieceIndex]. Returns the slot allocated to this piece.
     */
    fun capture(pieceIndex: Int): CapturedPieceSlot {
        slots.firstOrNull { it.pieceIndex == pieceIndex }?.let { return it }

        return slots.first { it.pieceIndex == null }.apply {
            this.pieceIndex = pieceIndex
        }
    }

    fun uncapture(pieceIndex: Int) {
        slots.firstOrNull { it.pieceIndex == pieceIndex }?.let { it.pieceIndex = null }
    }

    /**
     * Removes all captured pieces.
     */
    fun clear() {
        slots.forEach { it.pieceIndex = null }
    }
}

/**
 * Creates a new instance of [CapturedPieceHostState].
 *
 * @param slotCount the number of slots to be created.
 * This must be 2 times the number of pieces that can be captured for each player.
 * E.g. 32 for a standard game of Keizar.
 */
fun CapturedPieceHostState(
    // Actually only 16 pieces can be captured, 
    // but currently the game crashes on clicking "Next Round" so we use 32 here.
    slotCount: Int = 32,
): CapturedPieceHostState {
    return object : CapturedPieceHostState {
        override val slots: List<CapturedPieceSlot> =
            SnapshotStateList<CapturedPieceSlot>().apply {
                repeat(slotCount) {
                    add(CapturedPieceSlot())
                }
            }
    }
}

/**
 * A row that allocates space for the captured pieces.
 *
 * [CapturedPiecesHost] is designed to be used along with a board (a reference object in general).
 * Each [slot][CapturedPieceSlot] in the captured pieces host is positioned relative to the top-left corner of the board,
 * allowing [BoardPieces] to automatically move captured pieces to the slot.
 *
 * The [BoardTransitionController] can be used to animate captured pieces from their positions on the board to the slot.
 *
 * ## Example Usage with [BoardTransitionController]
 *
 * ```
 * // In composable:
 * val controller = remember { BoardTransitionController() }
 *
 * CapturedPiecesHost(
 *    capturedPieceHostState = controller.myCapturedPieceHostState, // or `.theirCapturedPieceHostState` for opponents
 *    slotSize = tileSize,
 *    sourceCoordinates = boardGlobalCoordinates,
 *    Modifier.fillMaxWidth()
 * )
 *
 * // In view model:
 * val pieceOffset = BoardTransitionController.pieceOffset(
 *    piece = piece,
 *    arrangedPos = pieceArranger.offsetFor(pos).collectAsStateWithLifecycle(DpOffset.Zero)
 * )
 *
 * // Called when rule engine thinks a piece is captured:
 * fun capturePiece(pieceIndex: Int) {
 *    // Marks that pieceIndex as captured,
 *    // then [BoardTransitionController] will change the piece's offset to the slot.
 *    capturedPieceHostState.capture(pieceIndex)
 * }
 * ```
 *
 * @param sourceCoordinates coordinates of a source component that will be used to calculate the offset of the slots.
 * This can typically be the offset of the top-left corner of the board in the parent layout.
 * @param slotSize the number of slots to be created.
 * This must be 2 times the number of pieces that can be captured for each player.
 * E.g. 32 for a standard game of Keizar.
 */
@Composable
fun CapturedPiecesHost(
    capturedPieceHostState: CapturedPieceHostState,
    slotSize: DpSize,
    sourceCoordinates: LayoutCoordinates?,
    modifier: Modifier = Modifier,
) {
    val density by rememberUpdatedState(newValue = LocalDensity.current)
    val sourceCoordinatesUpdated by rememberUpdatedState(newValue = sourceCoordinates)
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            // Note that all the slots are placed in a box, so they effectively only take one [slotSize].
            // We still need multiple slots for it to be easier to animate captured pieces 
            // from their positions on the board to the slot.

            for (item in capturedPieceHostState.slots) {
                key(sourceCoordinatesUpdated) { // Recompose when the source coordinates are updated
                    CapturedPieceSlot(
                        slotSize,
                        Modifier.onGloballyPositioned {
                            density.run {
                                item.offsetFromBoard = sourceCoordinatesUpdated?.let { source ->
                                    source.localPositionOf(it, Offset.Zero)
                                        .run { DpOffset(x.toDp(), y.toDp()) }
                                } ?: DpOffset.Zero
                            }
                        }
                    )
                }
            }
        }
        val capturedPieceCount = capturedPieceHostState.capturedPieceCount
        if (capturedPieceCount > 1) {
            Text(
                text = "x $capturedPieceCount",
                Modifier.offset(x = (-8).dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun CapturedPieceSlot(
    size: DpSize,
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = modifier.size(size))
}


@Preview(showBackground = true)
@Composable
private fun PreviewCapturedPieces() {
    val vm = rememberSinglePlayerGameBoardViewModel(
        buildGameSession {
            round {
                resetPieces {
                    black("a8", isCaptured = true)
                    black("a1", isCaptured = true)
                    black("a7")
                    white("a6", isCaptured = true)
                    white("a4", isCaptured = true)
                }
            }
            round { }
        },
        selfPlayer = Player.FirstWhitePlayer,
    )

    SideEffect {
        vm.theirCapturedPieceHostState.capture(0)
        vm.theirCapturedPieceHostState.capture(1)
    }

    var boardGlobalCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }

    val tileSize by vm.pieceArranger.tileSize.collectAsStateWithLifecycle(DpSize.Zero)
    Column(Modifier.width(400.dp)) {
        CapturedPiecesHost(
            capturedPieceHostState = vm.theirCapturedPieceHostState,
            slotSize = tileSize,
            sourceCoordinates = boardGlobalCoordinates,
            Modifier.fillMaxWidth()
        )

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .onGloballyPositioned { boardGlobalCoordinates = it }) {
            BoardBackground(vm, Modifier.matchParentSize())
            BoardPieces(vm)
            PossibleMovesOverlay(vm)
        }

        CapturedPiecesHost(
            capturedPieceHostState = vm.myCapturedPieceHostState,
            slotSize = tileSize,
            sourceCoordinates = boardGlobalCoordinates,
            Modifier.fillMaxWidth()
        )
    }

}