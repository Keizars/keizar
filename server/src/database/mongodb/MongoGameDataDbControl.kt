package org.keizar.server.database.mongodb
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList

import org.keizar.server.database.GameDataDBControl
import org.keizar.server.database.models.GameDataModel
import java.util.UUID

class MongoGameDataDbControl(
    private val gameDataTable: MongoCollection<GameDataModel>
): GameDataDBControl {
    override suspend fun addGameData(gameData: GameDataModel): Boolean {
        // add data if id not in list and username and time is not the same
        return gameDataTable.insertOne(gameData).wasAcknowledged()
    }

    override suspend fun removeGameData(gameDataId: UUID): Boolean {
        return gameDataTable.updateOne(
            Filters.eq("id", gameDataId),
            Updates.set("userSaved", false)
        ).wasAcknowledged()
    }

    override suspend fun getGameDataById(gameDataId: UUID): GameDataModel{
        return gameDataTable.find(
            Filters.eq("id", gameDataId)
        ).first()
    }

    override suspend fun getGameDataByUser(userId: String): List<GameDataModel> {
        val list = mutableListOf<GameDataModel>()
        gameDataTable.find(
            Filters.or(
                Filters.eq("userId1", userId),
                Filters.eq("userId2", userId),
                Filters.eq("userSaved", true)
            )
        ).toList(list)
        return list
    }

    override suspend fun saveGameData(dataId: UUID) {
        gameDataTable.updateOne(
            Filters.eq("id", dataId),
            Updates.set("userSaved", true)
        )
    }
}