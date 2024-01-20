package org.keizar.android.ui.game

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties


/**
 * Arranges the pieces on the board.
 */
interface PieceArranger {
    /**
     * Size of each tile.
     */
    val tileSize: Flow<DpSize>

    /**
     * Updates the total width and height of the board.
     */
    fun setDimensions(totalWidth: Dp, totalHeight: Dp)

    /**
     * Returns a flow of the offsets starting from the top-left corner of the board, for the [pos].
     */
    fun offsetFor(pos: Flow<BoardPos>): Flow<DpOffset>

    /**
     * Returns a flow of the offsets starting from the top-left corner of the board, for the [pos].
     */
    fun offsetFor(pos: BoardPos): Flow<DpOffset>

    /**
     * Returns the nearest [BoardPos] for the given [value], relative to the position [from].
     *
     * If [from] is `null`, the nearest position is calculated as an absolute offset from the top-left corner of the board.
     */
    fun getNearestPos(value: DpOffset, from: BoardPos? = null): Flow<BoardPos>
}

fun PieceArranger(
    boardProperties: BoardProperties,
): PieceArranger {
    return PieceArrangerImpl(boardProperties)
}

private class PieceArrangerImpl(
    private val boardProperties: BoardProperties,
) : PieceArranger {
    private val totalWidth: MutableSharedFlow<Dp> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val totalHeight: MutableSharedFlow<Dp> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun setDimensions(totalWidth: Dp, totalHeight: Dp) {
        this.totalWidth.tryEmit(totalWidth)
        this.totalHeight.tryEmit(totalHeight)
    }

    private val tileWidth = totalWidth.map { it / boardProperties.width }
    private val tileHeight = totalHeight.map { it / boardProperties.height }

    override val tileSize: Flow<DpSize> = combine(tileWidth, tileHeight) { width, height ->
        DpSize(width, height)
    }

    override fun offsetFor(pos: Flow<BoardPos>): Flow<DpOffset> {
        return combine(
            totalHeight,
            tileWidth, tileHeight,
            pos
        ) { boardHeight, tileWidth, tileHeight, p ->
            calculateOffset(boardHeight, tileWidth, tileHeight, p)
        }
    }

    override fun offsetFor(pos: BoardPos): Flow<DpOffset> {
        return combine(
            totalHeight,
            tileWidth, tileHeight,
        ) { boardHeight, tileWidth, tileHeight ->
            calculateOffset(boardHeight, tileWidth, tileHeight, pos)
        }
    }

    override fun getNearestPos(value: DpOffset, from: BoardPos?): Flow<BoardPos> {
        return combine(
            totalHeight,
            tileWidth, tileHeight,
        ) { boardHeight, tileWidth, tileHeight ->
            // center of tile
            val fromOffset = if (from == null) {
                DpOffset.Zero
            } else {
                calculateOffset(boardHeight, tileWidth, tileHeight, from)
            } + DpOffset(
                x = tileWidth / 2,
                y = tileHeight / 2,
            )

            val absoluteX = fromOffset.x + value.x
            val absoluteY = fromOffset.y + value.y

            BoardPos(
                row = boardProperties.height - ((absoluteY - absoluteY % tileHeight) / tileHeight).toInt() - 1,
                col = ((absoluteX - absoluteX % tileWidth) / tileWidth).toInt(),
            )
        }
    }

    private fun calculateOffset(
        boardHeight: Dp,
        tileWidth: Dp,
        tileHeight: Dp,
        p: BoardPos,
    ): DpOffset {
        val x = tileWidth * p.col
        val y = boardHeight - tileHeight * (p.row + 1) // from bottom to top
        return DpOffset(x, y)
    }
}

private operator fun Dp.rem(other: Dp): Dp = Dp(value % other.value)
