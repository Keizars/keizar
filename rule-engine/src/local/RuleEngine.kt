package org.keizar.game.local

import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.Player

class RuleEngine(
    private val boardProperties: BoardProperties,
    randomSeed: Int,
) {
    private val board = Board(boardProperties, randomSeed)
    private val movesLog = mutableListOf<Move>()
    private var winningCounter: Int = 0
    private var curPlayer: Player = boardProperties.startingPlayer
    private var winner: Player? = null

    fun showPossibleMoves(piece: Piece): List<BoardPos> {
        return board.showValidMoves(piece)
    }

    fun move(piece: Piece, dest: BoardPos): Boolean {
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
