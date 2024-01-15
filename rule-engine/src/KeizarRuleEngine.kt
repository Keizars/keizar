package org.keizar.game

import kotlinx.coroutines.flow.Flow
import org.keizar.game.local.Board

interface KeizarRuleEngine {
    val board: Board
    val win: Flow<Boolean>

    suspend fun undo(): Boolean
    suspend fun redo(): Boolean

    fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>>
    suspend fun move(from: BoardPos, to: BoardPos): Boolean
}

interface Board {
    val properties: BoardProperties
    
    fun pieceAt(pos: BoardPos): Flow<Player?>
}

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

class Move(
    val source: BoardPos,
    val dest: BoardPos,
    val isCapture: Boolean,
) {
    override fun toString(): String {
        return if (isCapture) "${source}-${dest}" else "${source}x${dest}"
    }
}
