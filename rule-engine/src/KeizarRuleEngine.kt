package org.keizar.game

import kotlinx.coroutines.flow.Flow

interface KeizarRuleEngine {
    val properties: BoardProperties

    fun pieceAt(pos: BoardPos): Flow<Player?>
    val win: Flow<Boolean>

    suspend fun undo(): Boolean
    suspend fun redo(): Boolean

    fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>>
    suspend fun move(from: BoardPos, to: BoardPos): Boolean
}
