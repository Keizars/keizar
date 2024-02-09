package org.keizar.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.keizar.game.internal.RuleEngine
import org.keizar.game.serialization.PieceSnapshot
import org.keizar.game.serialization.RoundSnapshot
import org.keizar.utils.communication.game.BoardPos

interface RoundSession {
    val pieces: List<Piece>

    val winner: StateFlow<Role?>
    val winningCounter: StateFlow<Int>
    val curRole: StateFlow<Role>
    val canUndo: StateFlow<Boolean>
    val canRedo: StateFlow<Boolean>

    // undo/redo: only available when playing against computer.
    // Players can only undo in their own turn before they make a move.
    // If they do so, the game returns to the state before the player makes their previous move
    // i.e. undo 2 turns, one computer turn and one player turn.
    suspend fun undo(role: Role): Boolean

    // Upon redo, the player recovers their own move and also the computer move right after it
    // i.e. redo 2 turns, one player turn and one computer turn.
    suspend fun redo(role: Role): Boolean

    fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>>
    fun getAllPiecesPos(role: Role): Flow<List<BoardPos>>
    suspend fun move(from: BoardPos, to: BoardPos): Boolean
    fun getLostPiecesCount(role: Role): StateFlow<Int>
    fun reset()
    fun getSnapshot(): RoundSnapshot = RoundSnapshot(
        winningCounter = winningCounter.value,
        curRole = curRole.value,
        winner = winner.value,
        pieces = pieces.map { it.getSnapShot() }
    )

    fun pieceAt(pos: BoardPos): Role?
}

class RoundSessionImpl(
    private val ruleEngine: RuleEngine,
) : RoundSession {
    override val pieces: List<Piece> = ruleEngine.pieces

    override val winner: StateFlow<Role?> = ruleEngine.winner

    override val winningCounter: StateFlow<Int> = ruleEngine.winningCounter

    override val curRole: StateFlow<Role> = ruleEngine.curRole

    override val canUndo: StateFlow<Boolean> = ruleEngine.canUndo

    override val canRedo: StateFlow<Boolean> = ruleEngine.canRedo

    override suspend fun undo(role: Role): Boolean {
        return ruleEngine.undo(role)
    }

    override suspend fun redo(role: Role): Boolean {
        return ruleEngine.redo(role)
    }

    override fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>> {
        return if (winner.value != null) {
            flowOf(listOf())
        } else {
            flowOf(ruleEngine.showPossibleMoves(from))
        }
    }

    override fun getAllPiecesPos(role: Role): Flow<List<BoardPos>> {
        return flowOf(ruleEngine.getAllPiecesPos(role))
    }

    override fun pieceAt(pos: BoardPos): Role? {
        return ruleEngine.pieceAt(pos)
    }

    override suspend fun move(from: BoardPos, to: BoardPos): Boolean {
        return if (winner.value != null) {
            false
        } else {
            ruleEngine.move(from, to)
        }
    }

    override fun getLostPiecesCount(role: Role): StateFlow<Int> {
        return ruleEngine.getLostPiecesCount(role)
    }

    override fun reset() {
        ruleEngine.reset()
    }
}
