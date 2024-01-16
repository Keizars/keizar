package org.keizar.android.ui.game

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.game.BoardPos
import org.keizar.game.GameSession
import org.keizar.game.Player
import org.keizar.game.boardPoses
import kotlin.random.Random

class GameBoardViewModel : AbstractViewModel() {
    private val game: GameSession = GameSession.create(Random)

    @Stable
    val pieces: Map<BoardPos, Piece> = game.properties.boardPoses.map { pos ->
        pos to Piece(
            player = game.pieceAt(pos).stateInBackground(),
        )
    }.toMap()

    private val _currentPick = MutableStateFlow<Pick?>(null)
    val currentPick: StateFlow<Pick?> = _currentPick.asStateFlow()

    private val currentPlayer: StateFlow<Player> = game.curPlayer.stateInBackground(Player.BLACK)

    /**
     * Currently available positions where the picked piece can move to. `null` if no piece is picked.
     */
    val availablePositions: SharedFlow<List<BoardPos>?> = currentPick.flatMapLatest { pick ->
        if (pick == null) {
            flowOf(emptyList())
        } else {
            game.getAvailableTargets(pick.pos)
        }
    }.shareInBackground()

    fun onClick(pos: BoardPos) {
        val pick = currentPick.value
        if (pick == null) {
            if (pieces[pos]?.player?.value != currentPlayer.value) return
            _currentPick.value = Pick(pos)
        } else {
            launchInBackground {
                game.move(pick.pos, pos)
                _currentPick.value = null
            }
        }
    }
}

@Stable
class Piece(
    val player: StateFlow<Player?>
)

@Immutable
class Pick(
    val pos: BoardPos,
)
