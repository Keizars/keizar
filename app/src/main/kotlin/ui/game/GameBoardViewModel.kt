package org.keizar.android.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpOffset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.utils.logging.info
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.Piece
import org.keizar.game.Player

interface GameBoardViewModel {
    @Stable
    val pieceArranger: PieceArranger

    @Stable
    val player: StateFlow<Player>

    /**
     * List of the pieces on the board.
     */
    @Stable
    val pieces: List<UiPiece>

    /**
     * Currently picked piece. `null` if no piece is picked.
     */
    @Stable
    val currentPick: StateFlow<Pick?>

    /**
     * Currently available positions where the picked piece can move to. `null` if no piece is picked.
     */
    @Stable
    val availablePositions: SharedFlow<List<BoardPos>?>


    /**
     * Called when the player single-clicks the piece.
     */
    fun onClickPiece(piece: UiPiece)

    /**
     * Called when the player single-clicks the empty tile.
     */
    fun onClickTile(pos: BoardPos)

    /**
     * Called when the player long-presses the piece.
     */
    fun onHold(piece: UiPiece)

    /**
     * Called when the player releases the piece after long-pressing it.
     */
    fun onRelease(piece: UiPiece)
}

@Composable
fun rememberGameBoardViewModel(boardProperties: BoardProperties): GameBoardViewModel {
    return remember {
        GameBoardViewModelImpl(boardProperties)
    }
}

private class GameBoardViewModelImpl(
    boardProperties: BoardProperties,
) : AbstractViewModel(), GameBoardViewModel {
    private val game: GameSession = GameSession.create(boardProperties)

    override val pieceArranger = PieceArranger(boardProperties = boardProperties)

    @Stable
    override val player: StateFlow<Player> = game.curPlayer

    @Stable
    override val pieces: List<UiPiece> = game.pieces.map {
        UiPiece(
            enginePiece = it,
            offsetInBoard = pieceArranger.offsetFor(it.pos).shareInBackground()
        )
    }

    @Stable
    override val currentPick: MutableStateFlow<Pick?> = MutableStateFlow(null)

    @Stable
    private val currentPlayer: StateFlow<Player> = game.curPlayer

    /**
     * Currently available positions where the picked piece can move to. `null` if no piece is picked.
     */
    @Stable
    override val availablePositions: SharedFlow<List<BoardPos>?> = currentPick.flatMapLatest { pick ->
        if (pick == null) {
            flowOf(emptyList())
        } else {
            game.getAvailableTargets(pick.pos)
        }
    }.shareInBackground()

    override fun onClickPiece(piece: UiPiece) {
        val currentPick = currentPick.value
        if (currentPick == null) {
            if (piece.player != currentPlayer.value) return
            this.currentPick.value = Pick(piece)
        } else {
            // Pick another piece
            launchInBackground {
                this@GameBoardViewModelImpl.currentPick.value = null
                movePiece(currentPick.piece.pos.value, piece.pos.value)
            }
            return
        }
    }

    override fun onClickTile(pos: BoardPos) {
        val pick = currentPick.value ?: return
        launchInBackground {
            this@GameBoardViewModelImpl.currentPick.value = null
            movePiece(pick.piece.pos.value, pos)
        }
    }

    private suspend fun movePiece(from: BoardPos, to: BoardPos) {
        game.move(from, to).also {
            logger.info { "[board] move $from to $to: $it" }
        }
    }

    override fun onHold(piece: UiPiece) {
        TODO("Not yet implemented")
    }

    override fun onRelease(piece: UiPiece) {
        TODO("Not yet implemented")
    }
}

@Immutable
class Pick(
    val piece: UiPiece,
) {
    val pos get() = piece.pos.value
}


/**
 * A wrapper of [Piece] that is aware of the UI states.
 */
@Stable
class UiPiece internal constructor(
    private val enginePiece: Piece,
    /**
     * The offset of the piece on the board, starting from the top-left corner.
     */
    val offsetInBoard: SharedFlow<DpOffset>
) : Piece by enginePiece 