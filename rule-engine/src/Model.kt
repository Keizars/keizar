package org.keizar.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Player {
    BLACK,
    WHITE;

    fun other(): Player = when (this) {
        BLACK -> WHITE
        WHITE -> BLACK
    }
}

enum class TileType {
    KING,
    QUEEN,
    BISHOP,
    KNIGHT,
    ROOK,
    KEIZAR,
    PLAIN,
}

enum class TileColor {
    BLACK,
    WHITE
}

class Move(
    val source: BoardPos,
    val dest: BoardPos,
    val isCapture: Boolean,
) {
    override fun toString(): String {
        return if (isCapture) "${source}x${dest}" else "${source}-${dest}"
    }
}

interface Piece {
    val index: Int
    val player: Player
    val pos: StateFlow<BoardPos>
    val isCaptured: StateFlow<Boolean>

    override fun toString(): String
}

class MutablePiece(
    override val index: Int,
    override val player: Player,
    override val pos: MutableStateFlow<BoardPos>,
    override val isCaptured: MutableStateFlow<Boolean> = MutableStateFlow(false),
) : Piece {
    override fun toString(): String {
        return "Piece(index=$index, player=$player, pos=${pos.value}, isCaptured=${isCaptured.value})"
    }
}

/**
 * Get a read-only view of this.
 */
fun MutablePiece.asPiece(): Piece = ReadOnlyPiece(this)

private class ReadOnlyPiece(
    private val piece: Piece,
) : Piece by piece