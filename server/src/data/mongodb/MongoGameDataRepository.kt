package org.keizar.server.data.mongodb
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList

import org.keizar.server.data.GameDataRepository
import org.keizar.server.data.models.GameDataModel
import java.util.UUID

class MongoGameDataRepository(
    private val gameDataTable: MongoCollection<GameDataModel>
): GameDataRepository {
    override suspend fun addGameData(gameData: GameDataModel): Boolean {
        // add data if id not in list and username and time is not the same
        return gameDataTable.insertOne(gameData).wasAcknowledged()
    }

    override suspend fun removeGameData(gameDataId: UUID): Boolean {
        return gameDataTable.updateOne(
            Filters.eq("_id", gameDataId),
            Updates.set("userSaved", false)
        ).wasAcknowledged()
    }

    override suspend fun getGameDataById(gameDataId: UUID): GameDataModel{
        return gameDataTable.find(
            Filters.eq("_id", gameDataId)
        ).first()
    }

    override suspend fun getGameDataByUser(userId: String): List<GameDataModel> {
        val list = mutableListOf<GameDataModel>()
        gameDataTable.find(
            Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("userSaved", true)
            )
        ).toList(list)
        return list
    }

    override suspend fun saveGameData(dataId: UUID): Boolean {
        gameDataTable.updateOne(
            Filters.eq("_id", dataId),
            Updates.set("userSaved", true)
        )
        val list = mutableListOf<GameDataModel>()
        return gameDataTable.find(
            Filters.and(
                Filters.eq("_id", dataId),
                Filters.eq("userSaved", true)
            )
        ).toList(list).isNotEmpty()
    }
}