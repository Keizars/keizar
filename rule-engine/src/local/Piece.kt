package org.keizar.game.local

import org.keizar.game.BoardPos

data class Piece(val color: Color, var pos: BoardPos) {
    enum class Color {
        BLACK, WHITE;

        fun other(): Color = when (this) {
            BLACK -> WHITE
            WHITE -> BLACK
        }
    }
}