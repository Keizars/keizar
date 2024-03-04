package org.keizar.server.modules

import org.keizar.server.database.DatabaseManager
import org.keizar.server.database.models.GameDataModel
import org.keizar.server.database.models.dataToModel
import org.keizar.utils.communication.game.GameDataGet
import org.keizar.utils.communication.game.GameDataStore
import org.keizar.utils.communication.game.jsonElementToRoundStats
import java.util.UUID


interface GameDataModule {
    suspend fun getGameDataByUsedID(userId: UUID): List<GameDataGet>
    suspend fun addGameData(gameData: GameDataStore): Boolean
    suspend fun removeGameData(id: UUID): Boolean
}

class GameDataModuleImpl (
    private val database: DatabaseManager,
) : GameDataModule {
    override suspend fun getGameDataByUsedID(userId: UUID): List<GameDataGet> {
        val gameDataModels = database.gameData.getGameDataByUser(userId.toString())
        return gameDataModels.map { modelToDataGet(it) }
    }

    override suspend fun addGameData(gameData: GameDataStore): Boolean {
        return database.gameData.addGameData(dataToModel( gameData) )
    }

    override suspend fun removeGameData(id: UUID): Boolean {
        return database.gameData.removeGameData(id)
    }

    private suspend fun modelToDataGet(model: GameDataModel): GameDataGet {
        val userId = model.userId
        val userName = database.user.getUserById(userId!!)?.username ?: "Unknown"
        val opponentId = model.opponentId
        val opponentName = database.user.getUserById(opponentId!!)?.username ?: "Unknown"
        return GameDataGet(
            userName,
            opponentName,
            model.timeStamp,
            model.gameConfiguration,
            jsonElementToRoundStats(model.round1Statistics),
            jsonElementToRoundStats(model.round2Statistics),
            model.id.toString(),
        )
    }
}