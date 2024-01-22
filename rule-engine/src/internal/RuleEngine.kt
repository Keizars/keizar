package org.keizar.game.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.Piece
import org.keizar.game.Player
import org.keizar.game.serialization.GameSnapshot

interface RuleEngine {
    val winningCounter: StateFlow<Int>
    val curPlayer: StateFlow<Player>
    val winner: StateFlow<Player?>
    val pieces: List<Piece>

    fun showPossibleMoves(pos: BoardPos): List<BoardPos>
    fun move(source: BoardPos, dest: BoardPos): Boolean
    fun pieceAt(pos: BoardPos): Player?
    fun getLostPiecesCount(player: Player): StateFlow<Int>
    fun getAllPiecesPos(player: Player): List<BoardPos>
}

class RuleEngineImpl private constructor(
    private val boardProperties: BoardProperties,
    private val board: Board,

    private val movesLog: MutableList<Move>,
    override val winningCounter: MutableStateFlow<Int>,
    override val curPlayer: MutableStateFlow<Player>,
    override val winner: MutableStateFlow<Player?>,

    private val lostPiecesCount: Map<Player, MutableStateFlow<Int>>
) : RuleEngine {

    constructor(boardProperties: BoardProperties, ruleEngineCore: RuleEngineCore) : this(
        boardProperties = boardProperties,
        board = Board(
            boardProperties, ruleEngineCore
        ),
        movesLog = mutableListOf<Move>(),
        winningCounter = MutableStateFlow(0),
        curPlayer = MutableStateFlow(boardProperties.startingPlayer),
        winner = MutableStateFlow(null),
        lostPiecesCount = mapOf(
            Player.WHITE to MutableStateFlow(0),
            Player.BLACK to MutableStateFlow(0),
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
        curPlayer.value = curPlayer.value.other()
        updateWinner()

        return true
    }

    private fun updateLostPieces(move: Move) {
        if (move.isCapture) {
            ++lostPiecesCount[curPlayer.value.other()]!!.value
        }
    }

    override fun pieceAt(pos: BoardPos): Player? {
        return board.pieceAt(pos)?.player
    }

    override fun getLostPiecesCount(player: Player): StateFlow<Int> {
        return lostPiecesCount[player]!!
    }

    override fun getAllPiecesPos(player: Player): List<BoardPos> {
        return board.getAllPiecesPos(player)
    }

    private fun isValidMove(piece: Piece, dest: BoardPos): Boolean {
        return piece.player == curPlayer.value && board.isValidMove(piece, dest)
    }

    private fun updateWinningCounter(move: Move) {
        if (board.havePieceInKeizar(curPlayer.value.other())) {
            ++winningCounter.value
        } else if (move.dest == boardProperties.keizarTilePos) {
            winningCounter.value = 0
        }
    }

    private fun updateWinner() {
        if (winningCounter.value == boardProperties.winningCount) {
            winner.value = curPlayer.value
        }
        if (board.noValidMoves(curPlayer.value)) {
            winner.value = if (board.havePieceInKeizar(curPlayer.value)) {
                curPlayer.value
            } else {
                curPlayer.value.other()
            }
        }
    }

    companion object {
        fun restore(gameSnapshot: GameSnapshot, ruleEngineCore: RuleEngineCore): RuleEngine {
            val board = Board(gameSnapshot.properties, ruleEngineCore)
            board.rearrangePieces(gameSnapshot.pieces)
            val whiteLostPieces = board.pieces.count { it.isCaptured.value && it.player == Player.WHITE }
            val blackLostPieces = board.pieces.count { it.isCaptured.value && it.player == Player.BLACK }
            return RuleEngineImpl(
                boardProperties = gameSnapshot.properties,
                board = board,
                movesLog = mutableListOf(),
                winningCounter = MutableStateFlow(gameSnapshot.winningCounter),
                curPlayer = MutableStateFlow(gameSnapshot.curPlayer),
                winner = MutableStateFlow(gameSnapshot.winner),
                lostPiecesCount = mapOf(
                    Player.WHITE to MutableStateFlow(whiteLostPieces),
                    Player.BLACK to MutableStateFlow(blackLostPieces),
                ),
            )
        }
    }
}
