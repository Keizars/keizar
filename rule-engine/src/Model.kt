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

data class Piece(
    val index: Int,
    val player: Player,
    val pos: StateFlow<BoardPos>,
    val isCaptured: StateFlow<Boolean> = MutableStateFlow(false),
)

data class MutablePiece(
    val index: Int,
    val player: Player,
    val pos: MutableStateFlow<BoardPos>,
    val isCaptured: MutableStateFlow<Boolean> = MutableStateFlow(false),
) {
    fun asPiece(): Piece = Piece(index, player, pos, isCaptured)
}
