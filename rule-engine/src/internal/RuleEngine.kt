package org.keizar.game.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.MoveCountered
import org.keizar.game.Piece
import org.keizar.game.Role
import org.keizar.game.snapshot.RoundSnapshot
import org.keizar.utils.communication.game.BoardPos

interface RuleEngine {
    val winningCounter: StateFlow<Int>
    val curRole: StateFlow<Role>
    val winner: StateFlow<Role?>
    val pieces: List<Piece>
    val canRedo: StateFlow<Boolean>
    val canUndo: StateFlow<Boolean>
    val isFreeMove: Boolean
    val disableWinner: Boolean

    fun showPossibleMoves(pos: BoardPos): List<BoardPos>
    fun move(source: BoardPos, dest: BoardPos): Boolean
    fun pieceAt(pos: BoardPos): Role?
    fun getLostPiecesCount(role: Role): StateFlow<Int>
    fun getAllPiecesPos(role: Role): List<BoardPos>
    fun reset()
    fun undo2Steps(role: Role): Boolean
    fun redo2Steps(role: Role): Boolean
    fun undo(): Boolean
    fun redo(): Boolean
}

internal class RuleEngineImpl private constructor(
    private val boardProperties: BoardProperties,
    private val board: Board,

    override val isFreeMove: Boolean,
    override val disableWinner: Boolean,
    private val movesLog: MutableList<MoveCountered> = mutableListOf(),
    override val winningCounter: MutableStateFlow<Int> = MutableStateFlow(0),
    override val curRole: MutableStateFlow<Role> = MutableStateFlow(boardProperties.startingRole),
    override val winner: MutableStateFlow<Role?> = MutableStateFlow(null),

    private val lostPiecesCount: Map<Role, MutableStateFlow<Int>> = mapOf(
        Role.WHITE to MutableStateFlow(0),
        Role.BLACK to MutableStateFlow(0),
    ),
) : RuleEngine {
    private val redoBuffer: MutableList<Move> = mutableListOf()

    constructor(
        boardProperties: BoardProperties,
        ruleEngineCore: RuleEngineCore,
        isFreeMove: Boolean = false,
        disableWinner: Boolean = false,
    ) : this(
        boardProperties = boardProperties,
        board = Board(boardProperties, ruleEngineCore),
        isFreeMove = isFreeMove,
        disableWinner = disableWinner,
    )

    override val pieces: List<Piece> = board.pieces
    override val canRedo: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val canUndo: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override fun showPossibleMoves(pos: BoardPos): List<BoardPos> {
        return board.pieceAt(pos)?.let { board.showValidMoves(it) } ?: listOf()
    }

    private fun move(source: BoardPos, dest: BoardPos, clearRedoBuffer: Boolean): Boolean {
        val piece = board.pieceAt(source) ?: return false
        if (!isValidMove(piece, dest)) {
            return false
        }

        if (isFreeMove) {
            curRole.value = piece.role
        }

        if (clearRedoBuffer) {
            redoBuffer.clear()
            canRedo.value = false
        }

        val move = board.move(source, dest)
        movesLog.add(MoveCountered(move, winningCounter.value))
        canUndo.value = movesLog.size >= 2
        updateLostPieces(move)
        updateWinningCounter(move)
        curRole.value = curRole.value.other()
        updateWinner()
        updateWinnerWhenNoMove()
        return true
    }

    override fun move(source: BoardPos, dest: BoardPos): Boolean =
        move(source, dest, clearRedoBuffer = true)

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
        redoBuffer.clear()
        canUndo.value = false
        canRedo.value = false
        winningCounter.value = 0
        curRole.value = boardProperties.startingRole
        winner.value = null
        lostPiecesCount.forEach { (_, flow) -> flow.value = 0 }
    }

    override fun undo2Steps(role: Role): Boolean {
        if (movesLog.size < 2) return false
        // only allow undo if the last move is made by opponent
        if (pieceAt(movesLog.last().move.dest) != role.other()) return false

        for (repeat in 0..1) {
            undo()
        }

        canUndo.value = movesLog.size >= 2
        canRedo.value = redoBuffer.size >= 2
        return true
    }

    override fun undo(): Boolean {
        if (movesLog.isEmpty()) return false
        val lastMove = movesLog.last()
        movesLog.removeLast()
        if (!board.undo(lastMove.move)) throw IllegalStateException("Undo unexpectedly failed")
        winningCounter.value = lastMove.counterValue
        redoBuffer.add(lastMove.move)
        curRole.value = curRole.value.other()
        return true
    }

    override fun redo2Steps(role: Role): Boolean {
        if (redoBuffer.size < 2) return false
        // only allow redo if the next move is to make by the player
        if (pieceAt(redoBuffer.last().source) != role) return false

        for (repeat in 0..1) {
            redo()
        }
        canRedo.value = redoBuffer.size >= 2
        return true
    }

    override fun redo(): Boolean {
        if (redoBuffer.isEmpty()) return false
        val nextMove = redoBuffer.last()
        redoBuffer.removeLast()
        move(nextMove.source, nextMove.dest, clearRedoBuffer = false)
        return true
    }

    private fun isValidMove(piece: Piece, dest: BoardPos): Boolean {
        return (isFreeMove || piece.role == curRole.value) && board.isValidMove(piece, dest)
    }

    private fun updateWinningCounter(move: Move) {
        if (board.havePieceInKeizar(curRole.value.other())) {
            ++winningCounter.value
        } else if (move.dest == boardProperties.keizarTilePos) {
            winningCounter.value = 0
        }
    }

    private fun updateWinner() {
        if (disableWinner) return
        if (winningCounter.value == boardProperties.winningCount) {
            winner.value = curRole.value
        }
    }

    private fun updateWinnerWhenNoMove() {
        if (disableWinner) return
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
                isFreeMove = roundSnapshot.isFreeMove,
                disableWinner = roundSnapshot.disableWinner,
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
