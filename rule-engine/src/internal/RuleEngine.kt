package org.keizar.game.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.Player

interface RuleEngine {
    val winningCounter: StateFlow<Int>
    val curPlayer: StateFlow<Player>
    val winner: StateFlow<Player?>

    fun showPossibleMoves(pos: BoardPos): List<BoardPos>
    fun move(source: BoardPos, dest: BoardPos): Boolean
    fun pieceAt(pos: BoardPos): Player?
    fun getLostPiecesCount(player: Player): StateFlow<Int>
    fun getAllPiecesPos(player: Player): List<BoardPos>
}

class RuleEngineImpl(
    private val boardProperties: BoardProperties,
    ruleEngineCore: RuleEngineCore,
) : RuleEngine {
    private val board = Board(boardProperties, ruleEngineCore)

    private val movesLog = mutableListOf<Move>()
    override val winningCounter: MutableStateFlow<Int> = MutableStateFlow(0)
    override val curPlayer: MutableStateFlow<Player> =
        MutableStateFlow(boardProperties.startingPlayer)
    override val winner: MutableStateFlow<Player?> = MutableStateFlow(null)
    private val lostPiecesCount: Map<Player, MutableStateFlow<Int>> = mapOf(
        Player.WHITE to MutableStateFlow(0),
        Player.BLACK to MutableStateFlow(0),
    )

    override fun showPossibleMoves(pos: BoardPos): List<BoardPos> {
        return board.pieceAt(pos)?.let { board.showValidMoves(it) } ?: listOf()
    }

    override fun move(source: BoardPos, dest: BoardPos): Boolean {
        val piece = board.pieceAt(source) ?: return false
        if (!isValidMove(piece, dest)) {
            return false
        }

        val move = board.move(piece, dest)
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
}
