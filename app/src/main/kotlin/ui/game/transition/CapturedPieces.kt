package org.keizar.android.ui.game.transition

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize

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

interface CapturedPieceHostState {
    /**
     * Slots of the captured pieces where captured pieces can be placed in.
     */
    @Stable
    val slots: List<CapturedPieceSlot>

    /**
     * Attempts to capture the piece with the given [pieceIndex]. Returns the slot allocated to this piece.
     */
    fun capture(pieceIndex: Int): CapturedPieceSlot {
        slots.firstOrNull { it.pieceIndex == pieceIndex }?.let { return it }

        return slots.first { it.pieceIndex == null }.apply {
            this.pieceIndex = pieceIndex
        }
    }

    fun clear() {
        slots.forEach { it.pieceIndex = null }
    }
}

fun CapturedPieceHostState(
    slotCount: Int = 20,
): CapturedPieceHostState {
    return object : CapturedPieceHostState {
        override val slots: List<CapturedPieceSlot> = List(slotCount) { CapturedPieceSlot() }
    }
}

/**
 * A row that allocates space for the captured pieces.
 *
 * @param sourceCoordinates coordinates of a source component that will be used to calculate the offset of the slots.
 * This can typically be the offset of the top-left corner of the board in the parent layout.
 */
@Composable
fun CapturedPiecesHost(
    capturedPieceHostState: CapturedPieceHostState,
    slotSize: DpSize,
    sourceCoordinates: LayoutCoordinates?,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
) {
    val density by rememberUpdatedState(newValue = LocalDensity.current)
    val sourceCoordinatesUpdated by rememberUpdatedState(newValue = sourceCoordinates)
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
}

@Composable
private fun CapturedPieceSlot(
    size: DpSize,
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = modifier.size(size))
}