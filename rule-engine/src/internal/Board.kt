package org.keizar.game.internal

import kotlinx.coroutines.flow.MutableStateFlow
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.MutablePiece
import org.keizar.game.Piece
import org.keizar.game.Role
import org.keizar.game.TileType
import org.keizar.game.asPiece
import org.keizar.game.snapshot.PieceSnapshot
import org.keizar.utils.communication.game.BoardPos

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

    fun rearrangePieces(pieces: List<PieceSnapshot>) {
        tiles.map { it.piece = null }
        _pieces.clear()
        for (pieceSnapshot in pieces) {
            val piece = MutablePiece.restore(pieceSnapshot)
            _pieces.add(piece)
            if (!piece.isCaptured.value) tileAt(piece.pos.value).piece = piece.asPiece()
        }
    }

    fun resetPieces() {
        tiles.map { it.piece = null }
        var index = 0
        for ((_, startingPos) in boardProperties.piecesStartingPos) {
            for (pos in startingPos) {
                val piece = _pieces[index++]
                piece.pos.value = pos
                piece.isCaptured.value = false
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

    fun havePieceInKeizar(role: Role): Boolean {
        return pieceAt(boardProperties.keizarTilePos)?.role == role
    }

    fun noValidMoves(role: Role): Boolean {
        for (piece in _pieces.filter { it.role == role && !it.isCaptured.value }) {
            if (showValidMoves(piece.asPiece()).isNotEmpty()) {
                return false
            }
        }
        return true
    }

    fun isValidMove(piece: Piece, dest: BoardPos): Boolean {
        return dest in showValidMoves(piece)
    }

    fun showValidMoves(piece: Piece): List<BoardPos> {
        return ruleEngineCore.showValidMoves(tiles, piece) { index }
    }

    fun getAllPiecesPos(role: Role): List<BoardPos> {
        return _pieces.filter { it.role == role && !it.isCaptured.value }.map { it.pos.value }
    }

    fun undo(move: Move): Boolean {
        val targetPiece = pieceAt(move.dest) ?: return false
        if (pieceAt(move.source) != null) return false

        if (!move.isCapture) {
            tileAt(move.dest).piece = null
        } else {
            val recoveredPiece = _pieces.firstOrNull {
                it.role == targetPiece.role.other() && it.pos.value == move.dest && it.isCaptured.value
            } ?: return false
            recoveredPiece.isCaptured.value = false
            tileAt(move.dest).piece = recoveredPiece.asPiece()
        }

        _pieces[targetPiece.index].pos.value = move.source
        tileAt(move.source).piece = targetPiece
        return true
    }
}