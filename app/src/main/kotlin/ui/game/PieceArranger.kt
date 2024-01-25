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
import org.keizar.game.Role


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
     * Returns a flow of the offsets starting from the top-left corner of the board, for the **logical** [pos].
     */
    fun offsetFor(pos: Flow<BoardPos>): Flow<DpOffset>

    /**
     * Returns a flow of the offsets starting from the top-left corner of the board, for the **logical** [pos].
     */
    fun offsetFor(pos: BoardPos): Flow<DpOffset>

    /**
     * Returns the nearest [BoardPos] for the given [value], relative to the position [from].
     *
     * If [from] is `null`, the nearest position is calculated as an absolute offset from the top-left corner of the board.
     *
     * @return the nearest **logical** [BoardPos].
     */
    fun getNearestPos(value: DpOffset, from: BoardPos? = null): Flow<BoardPos>

    /**
     * Returns a flow of the **logical** [BoardPos] for the given **view** [viewPos].
     */
    fun viewToLogical(viewPos: BoardPos): Flow<BoardPos>

    /**
     * Returns a flow of the **logical** [BoardPos] for the given **view** [viewPos].
     */
    fun viewToLogical(viewPos: Flow<BoardPos>): Flow<BoardPos>

    /**
     * Returns a flow of the **view** [BoardPos] for the given **logical** [logicalPos].
     */
    fun logicalToView(logicalPos: BoardPos): Flow<BoardPos>
}

/**
 * Creates a [PieceArranger] for the given [boardProperties] and [player][viewedAs].
 * @param viewedAs the player who is viewing the board.
 * If it is [Role.WHITE], the white pieces will be placed to the bottom of the board.
 * If it is [Role.BLACK], the black pieces will be placed to the bottom of the board.
 */
fun PieceArranger(
    boardProperties: BoardProperties,
    viewedAs: Flow<Role>,
): PieceArranger {
    return PieceArrangerImpl(boardProperties, viewedAs)
}

private class PieceArrangerImpl(
    private val boardProperties: BoardProperties,
    private val role: Flow<Role>,
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
            pos,
            role,
        ) { boardHeight, tileWidth, tileHeight, p, player ->
            calculateOffset(boardHeight, tileWidth, tileHeight, p.adjustedFor(player))
        }
    }

    override fun offsetFor(pos: BoardPos): Flow<DpOffset> {
        return combine(
            totalHeight,
            tileWidth, tileHeight,
            role,
        ) { boardHeight, tileWidth, tileHeight, player ->
            calculateOffset(boardHeight, tileWidth, tileHeight, pos.adjustedFor(player))
        }
    }

    override fun getNearestPos(value: DpOffset, from: BoardPos?): Flow<BoardPos> {
        return combine(
            totalHeight,
            tileWidth, tileHeight,
            role,
        ) { boardHeight, tileWidth, tileHeight, player ->
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
            ).adjustedFor(player)
        }
    }

    override fun viewToLogical(viewPos: BoardPos): Flow<BoardPos> {
        return role.map {
            viewPos.adjustedFor(it)
        }
    }

    override fun viewToLogical(viewPos: Flow<BoardPos>): Flow<BoardPos> {
        return role.combine(viewPos) { it, vp ->
            vp.adjustedFor(it)
        }
    }

    override fun logicalToView(logicalPos: BoardPos): Flow<BoardPos> {
        return role.map {
            logicalPos.adjustedFor(it)
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

    private fun BoardPos.adjustedFor(role: Role): BoardPos {
        return if (role == Role.WHITE) {
            this
        } else {
            BoardPos(
                row = boardProperties.height - row - 1,
                col = boardProperties.width - col - 1,
            )
        }
    }
}

private operator fun Dp.rem(other: Dp): Dp = Dp(value % other.value)
