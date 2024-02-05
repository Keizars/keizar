package org.keizar.game.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.Piece
import org.keizar.game.Role
import org.keizar.game.serialization.RoundSnapshot
import org.keizar.utils.communication.game.BoardPos

interface RuleEngine {
    val winningCounter: StateFlow<Int>
    val curRole: StateFlow<Role>
    val winner: StateFlow<Role?>
    val pieces: List<Piece>

    fun showPossibleMoves(pos: BoardPos): List<BoardPos>
    fun move(source: BoardPos, dest: BoardPos): Boolean
    fun pieceAt(pos: BoardPos): Role?
    fun getLostPiecesCount(role: Role): StateFlow<Int>
    fun getAllPiecesPos(role: Role): List<BoardPos>
    fun reset()
}

class RuleEngineImpl private constructor(
    private val boardProperties: BoardProperties,
    private val board: Board,

    private val movesLog: MutableList<Move>,
    override val winningCounter: MutableStateFlow<Int>,
    override val curRole: MutableStateFlow<Role>,
    override val winner: MutableStateFlow<Role?>,

    private val lostPiecesCount: Map<Role, MutableStateFlow<Int>>
) : RuleEngine {

    constructor(boardProperties: BoardProperties, ruleEngineCore: RuleEngineCore) : this(
        boardProperties = boardProperties,
        board = Board(
            boardProperties, ruleEngineCore
        ),
        movesLog = mutableListOf<Move>(),
        winningCounter = MutableStateFlow(0),
        curRole = MutableStateFlow(boardProperties.startingRole),
        winner = MutableStateFlow(null),
        lostPiecesCount = mapOf(
            Role.WHITE to MutableStateFlow(0),
            Role.BLACK to MutableStateFlow(0),
        ),
    )

    override val pieces: List<Piece> = board.pieces

    override fun showPossibleMoves(pos: BoardPos): List<BoardPos> {
        return board.pieceAt(pos)?.let { board.showValidMoves(it) } ?: listOf()
    }

    override fun move(source: BoardPos, dest: BoardPos): Boolean {
        val piece = board.pieceAt(source) ?: return false
        if (!isValidMove(piece, dest)) {
            return false
        }

        val move = board.move(source, dest)
        movesLog.add(move)
        updateLostPieces(move)
        updateWinningCounter(move)
        updateWinnerWhenNoMove()
        curRole.value = curRole.value.other()
        updateWinner()

        return true
    }

    private fun updateLostPieces(move: Move) {
        if (move.isCapture) {
            ++lostPiecesCount[curRole.value.other()]!!.value
        }
    }

    override fun pieceAt(pos: BoardPos): Role? {
        return board.pieceAt(pos)?.role
    }

    override fun getLostPiecesCount(role: Role): StateFlow<Int> {
        return lostPiecesCount[role]!!
    }

    override fun getAllPiecesPos(role: Role): List<BoardPos> {
        return board.getAllPiecesPos(role)
    }

    override fun reset() {
        board.resetPieces()
        movesLog.clear()
        winningCounter.value = 0
        curRole.value = boardProperties.startingRole
        winner.value = null
        lostPiecesCount.forEach { (_, flow) -> flow.value = 0 }
    }

    private fun isValidMove(piece: Piece, dest: BoardPos): Boolean {
        return piece.role == curRole.value && board.isValidMove(piece, dest)
    }

    private fun updateWinningCounter(move: Move) {
        if (board.havePieceInKeizar(curRole.value.other())) {
            ++winningCounter.value
        } else if (move.dest == boardProperties.keizarTilePos) {
            winningCounter.value = 0
        }
    }

    private fun updateWinner() {
        if (winningCounter.value == boardProperties.winningCount) {
            winner.value = curRole.value
        }
    }

    private fun updateWinnerWhenNoMove() {
        if (board.noValidMoves(curRole.value)) {
            winner.value = if (board.havePieceInKeizar(curRole.value)) {
                curRole.value
            } else {
                curRole.value.other()
            }
        }
    }

    companion object {
        fun restore(
            properties: BoardProperties,
            roundSnapshot: RoundSnapshot,
            ruleEngineCore: RuleEngineCore
        ): RuleEngine {
            val board = Board(properties, ruleEngineCore)
            board.rearrangePieces(roundSnapshot.pieces)
            val whiteLostPieces =
                board.pieces.count { it.isCaptured.value && it.role == Role.WHITE }
            val blackLostPieces =
                board.pieces.count { it.isCaptured.value && it.role == Role.BLACK }
            return RuleEngineImpl(
                boardProperties = properties,
                board = board,
                movesLog = mutableListOf(),
                winningCounter = MutableStateFlow(roundSnapshot.winningCounter),
                curRole = MutableStateFlow(roundSnapshot.curRole),
                winner = MutableStateFlow(roundSnapshot.winner),
                lostPiecesCount = mapOf(
                    Role.WHITE to MutableStateFlow(whiteLostPieces),
                    Role.BLACK to MutableStateFlow(blackLostPieces),
                ),
            )
        }
    }
}
