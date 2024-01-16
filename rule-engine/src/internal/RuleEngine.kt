package org.keizar.game.local

import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.Player
import org.keizar.game.internal.RuleEngineCore

interface RuleEngine {
    val winningCounter: Int
    val curPlayer: Player
    val winner: Player?

    fun showPossibleMoves(pos: BoardPos): List<BoardPos>
    fun move(source: BoardPos, dest: BoardPos): Boolean
    fun pieceAt(pos: BoardPos): Player?
}

class RuleEngineImpl(
    private val boardProperties: BoardProperties,
    ruleEngineCore: RuleEngineCore,
) : RuleEngine {
    private val board = Board(boardProperties, ruleEngineCore)

    private val movesLog = mutableListOf<Move>()
    override var winningCounter: Int = 0
        internal set
    override var curPlayer: Player = boardProperties.startingPlayer
        internal set
    override var winner: Player? = null
        internal set

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
        curPlayer = curPlayer.other()
        updateWinningCounter()
        updateWinner()

        return true
    }

    override fun pieceAt(pos: BoardPos): Player? {
        return board.pieceAt(pos)?.player
    }

    private fun isValidMove(piece: Piece, dest: BoardPos): Boolean {
        return piece.player == curPlayer && board.isValidMove(piece, dest)
    }

    private fun updateWinningCounter() {
        if (board.havePieceInKeizar(curPlayer)) {
            ++winningCounter
        } else {
            winningCounter = 0
        }
    }

    private fun updateWinner() {
        if (winningCounter == boardProperties.winningCount) {
            winner = curPlayer
        }
        if (board.noValidMoves(curPlayer)) {
            winner = if (board.havePieceInKeizar(curPlayer)) curPlayer else curPlayer.other()
        }
    }
}
