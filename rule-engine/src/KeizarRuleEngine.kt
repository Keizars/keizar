package org.keizar.game

import kotlinx.coroutines.flow.Flow

interface KeizarRuleEngine {
    val board: Board

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
    val width: Int
    val height: Int

    val winningTile: Tile

    val tiles: Flow<List<Tile>>
}π