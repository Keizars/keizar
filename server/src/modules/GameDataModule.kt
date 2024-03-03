package org.keizar.server.modules

import org.keizar.server.database.DatabaseManager
import org.keizar.server.database.models.dataToModel
import org.keizar.utils.communication.game.GameData
import java.util.UUID


interface GameDataModule {
    suspend fun getGameData(userId: UUID): List<GameData>
    suspend fun addGameData(gameData: GameData): Boolean
    suspend fun removeGameData(id: UUID): Boolean
}

class GameDataModuleImpl (
    private val database: DatabaseManager,
) : GameDataModule {
    override suspend fun getGameData(userId: UUID): List<GameData> {
        return database.gameData.getGameDataByUser(userId.toString())
    }

    override suspend fun addGameData(gameData: GameData): Boolean {
        return database.gameData.addGameData(dataToModel( gameData) )
    }

    override suspend fun removeGameData(id: UUID): Boolean {
        return database.gameData.removeGameData(id)
    }
}