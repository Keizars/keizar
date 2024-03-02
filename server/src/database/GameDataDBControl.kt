package org.keizar.server.database

import org.keizar.server.database.models.GameDataModel
import java.util.UUID

interface GameDataDBControl {

    suspend fun addGameData(gameData: GameDataModel): Boolean

    suspend fun removeGameData(gameDataId: UUID): Boolean

    suspend fun getGameDataById(gameDataId: String): GameDataModel

    suspend fun getGameDataByUser(userId: String): List<GameDataModel>

    suspend fun getGameDataByUserAndOpponent(
        userId1: String,
        userId2: String,
    ): GameDataModel

}