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

    val tiles: Flow<List<BoardPos>>
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