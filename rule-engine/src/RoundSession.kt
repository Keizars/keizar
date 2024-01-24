package org.keizar.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.keizar.game.internal.RuleEngine
import org.keizar.game.serialization.PieceSnapshot
import org.keizar.game.serialization.RoundSnapshot

interface RoundSession {
    val pieces: List<Piece>

    val winner: StateFlow<Role?>
    val winningCounter: StateFlow<Int>
    val curRole: StateFlow<Role>

    suspend fun undo(): Boolean
    suspend fun redo(): Boolean

    fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>>
    fun getAllPiecesPos(role: Role): Flow<List<BoardPos>>
    suspend fun move(from: BoardPos, to: BoardPos): Boolean
    fun getLostPiecesCount(role: Role): StateFlow<Int>
    fun getSnapshot(): RoundSnapshot = RoundSnapshot(
        winningCounter = winningCounter.value,
        curRole = curRole.value,
        winner = winner.value,
        pieces = pieces.map { it.getSnapShot() }
    )
}

class RoundSessionImpl(
    private val ruleEngine: RuleEngine,
) : RoundSession {
    override val pieces: List<Piece> = ruleEngine.pieces

    override val winner: StateFlow<Role?> = ruleEngine.winner

    override val winningCounter: StateFlow<Int> = ruleEngine.winningCounter

    override val curRole: StateFlow<Role> = ruleEngine.curRole

    override suspend fun undo(): Boolean {
        // TODO("Not yet implemented")
        return true
    }

    override suspend fun redo(): Boolean {
        // TODO("Not yet implemented")
        return true
    }

    override fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>> {
        return flowOf(ruleEngine.showPossibleMoves(from))
    }

    override fun getAllPiecesPos(role: Role): Flow<List<BoardPos>> {
        return flowOf(ruleEngine.getAllPiecesPos(role))
    }

    override suspend fun move(from: BoardPos, to: BoardPos): Boolean {
        return ruleEngine.move(from, to)
    }

    override fun getLostPiecesCount(role: Role): StateFlow<Int> {
        return ruleEngine.getLostPiecesCount(role)
    }
}