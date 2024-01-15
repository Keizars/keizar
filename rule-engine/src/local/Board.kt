package org.keizar.game.local

import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties

class Board(
    private val boardProperties: BoardProperties,
    seed: Int
) {
    private lateinit var tiles: List<Tile>
    private val pieces: MutableList<Piece> = mutableListOf()

    private val BoardPos.index get() = row * boardProperties.width + col

    init {
        randomInitTiles(seed)
        initPieces()
    }

    private fun randomInitTiles(seed: Int) {
        // TODO("Not yet implemented")
        val temp = mutableListOf<Tile>()
        for (i in 0..<(boardProperties.width * boardProperties.height)) {
            temp.add(Tile(Tile.Symbol.PLAIN))
        }
        tiles = temp
    }

    private fun initPieces() {
        for ((color, startingPos) in boardProperties.piecesStartingPos) {
            for (pos in startingPos) {
                val piece = Piece(color, pos)
                pieces.add(piece)
                tileAt(pos).piece = piece
            }
        }
    }

    private fun tileAt(pos: BoardPos): Tile {
        return tiles[pos.index]
    }

    private fun pieceAt(pos: BoardPos): Piece? {
        return tiles[pos.index].piece
    }

    fun move(piece: Piece, dest: BoardPos): Move {
        // assume the move is valid
        val targetPiece = pieceAt(dest)
        val source = piece.pos
        val isCapture = targetPiece != null
        if (isCapture) {
            pieces.remove(targetPiece)
        }

        tileAt(source).piece = null
        tileAt(dest).piece = piece
        piece.pos = dest

        return Move(source, dest, isCapture)
    }

    fun havePieceInKeizar(player: Piece.Color): Boolean {
        return pieceAt(boardProperties.winningPos)?.color == player
    }

    fun noValidMoves(player: Piece.Color): Boolean {
        for (piece in pieces.filter { it.color == player }) {
            if (showValidMoves(piece).isNotEmpty()) return false
        }
        return true
    }

    fun isValidMove(piece: Piece, dest: BoardPos): Boolean {
        return dest in showValidMoves(piece)
    }

    fun showValidMoves(piece: Piece): List<BoardPos> {
        // TODO("Not yet implemented")
        return (0..boardProperties.width).flatMap { row ->
            (0..boardProperties.height).map { col ->
                BoardPos(row, col)
            }
        }
    }
}