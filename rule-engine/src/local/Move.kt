package org.keizar.game.local

import org.keizar.game.BoardPos

class Move(
    val source: BoardPos,
    val dest: BoardPos,
    val isCapture: Boolean,
) {
    override fun toString(): String {
        return if (isCapture) "${source}-${dest}" else "${source}x${dest}"
    }
}