package org.keizar.game.internal

import kotlinx.coroutines.flow.MutableStateFlow
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.MutablePiece
import org.keizar.game.Piece
import org.keizar.game.Player
import org.keizar.game.TileType
import org.keizar.game.asPiece

class Tile(val type: TileType) {
    var piece: Piece? = null
}

class Board(
    private val boardProperties: BoardProperties,
    private val ruleEngineCore: RuleEngineCore,
) {
    private val tiles: List<Tile>
    private val _pieces: MutableList<MutablePiece> = mutableListOf()
    val pieces: List<Piece> get() = _pieces.map { it.asPiece() }

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
        var index = 0
        for ((color, startingPos) in boardProperties.piecesStartingPos) {
            for (pos in startingPos) {
                val piece = MutablePiece(index++, color, MutableStateFlow(pos))
                _pieces.add(piece)
                tileAt(pos).piece = piece.asPiece()
            }
        }
    }

    private fun tileAt(pos: BoardPos): Tile {
        return tiles[pos.index]
    }

    fun pieceAt(pos: BoardPos): Piece? {
        return tiles[pos.index].piece
    }

    fun move(source: BoardPos, dest: BoardPos): Move {
        // assume the move is valid
        val piece = pieceAt(source)!!.let { _pieces[it.index] }
        val targetPiece = pieceAt(dest)?.let { _pieces[it.index] }
        val isCapture = targetPiece != null
        if (isCapture) {
            targetPiece?.isCaptured?.value = true
        }

        tileAt(source).piece = null
        tileAt(dest).piece = piece.asPiece()
        piece.pos.value = dest

        return Move(source, dest, isCapture)
    }

    fun havePieceInKeizar(player: Player): Boolean {
        return pieceAt(boardProperties.keizarTilePos)?.player == player
    }

    fun noValidMoves(player: Player): Boolean {
        for (piece in _pieces.filter { it.player == player }) {
            if (showValidMoves(piece.asPiece()).isNotEmpty()) return false
        }
        return true
    }

    fun isValidMove(piece: Piece, dest: BoardPos): Boolean {
        return dest in showValidMoves(piece)
    }

    fun showValidMoves(piece: Piece): List<BoardPos> {
        return ruleEngineCore.showValidMoves(tiles, piece) { index }
    }

    fun getAllPiecesPos(player: Player): List<BoardPos> {
        return _pieces.filter { it.player == player && !it.isCaptured.value }.map { it.pos.value }
    }
}