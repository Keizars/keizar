package org.keizar.game

import kotlinx.coroutines.flow.Flow

interface KeizarRuleEngine {
    val board: Board
    val win: Flow<Boolean>

    suspend fun undo(): Boolean
    suspend fun redo(): Boolean

    fun getAvailableTargets(from: Tile): Flow<List<Tile>>
    suspend fun move(from: Tile, to: Tile): Boolean
}

data class Tile(
    val row: Int,
    val column: Int,
)

interface Board {
    val properties: BoardProperties

    val tiles: Flow<List<Tile>>
}

class BoardProperties(
    val width: Int,
    val height: Int,
    val winningTile: Tile,
)
