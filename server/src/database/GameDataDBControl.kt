package org.keizar.server.database

import org.keizar.server.database.models.GameDataModel
import org.keizar.utils.communication.game.GameData
import java.util.UUID

interface GameDataDBControl {

    suspend fun addGameData(gameData: GameDataModel): Boolean

    suspend fun removeGameData(gameDataId: UUID): Boolean

    suspend fun getGameDataById(gameDataId: UUID): GameData

    suspend fun getGameDataByUser(userId: String): List<GameData>


}