package org.keizar.game.internal

import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.Player
import org.keizar.game.TileType

data class Piece(val player: Player, var pos: BoardPos)

class Tile(val type: TileType) {
    var piece: Piece?= null
}

class Board(
    private val boardProperties: BoardProperties,
    private val ruleEngineCore: RuleEngineCore,
) {
    private val tiles: List<Tile>
    private val pieces: MutableList<Piece> = mutableListOf()

    private val BoardPos.index get() = row * boardProperties.width + col

    init {
        val tempTiles = MutableList(boardProperties.width * boardProperties.height) {
            Tile(TileType.PLAIN)
        }
        boardProperties.tileArrangement.toList().map { (pos, type) ->
            tempTiles[pos.index] = Tile(type)
        }
        tiles = tempTiles
        initPieces()
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

    fun pieceAt(pos: BoardPos): Piece? {
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

    fun havePieceInKeizar(player: Player): Boolean {
        return pieceAt(boardProperties.keizarTilePos)?.player == player
    }

    fun noValidMoves(player: Player): Boolean {
        for (piece in pieces.filter { it.player == player }) {
            if (showValidMoves(piece).isNotEmpty()) return false
        }
        return true
    }

    fun isValidMove(piece: Piece, dest: BoardPos): Boolean {
        return dest in showValidMoves(piece)
    }

    fun showValidMoves(piece: Piece): List<BoardPos> {
        return ruleEngineCore.showValidMoves(tiles, piece) { index }
    }
}