package org.keizar.server.database

import org.keizar.server.database.models.GameDataModel
import java.util.UUID

interface GameDataRepository {

    suspend fun addGameData(gameData: GameDataModel): Boolean

    suspend fun removeGameData(gameDataId: UUID): Boolean

    suspend fun getGameDataById(gameDataId: UUID): GameDataModel

    suspend fun getGameDataByUser(userId: String): List<GameDataModel>

    suspend fun saveGameData(dataId: UUID): Boolean


}