package org.keizar.game

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
