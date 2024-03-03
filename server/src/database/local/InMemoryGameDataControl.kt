package org.keizar.server.database.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.server.database.GameDataDBControl
import org.keizar.server.database.models.GameDataModel
import org.keizar.server.database.models.modelToData
import org.keizar.utils.communication.game.GameData
import java.util.UUID

class InMemoryGameDataControl: GameDataDBControl {
    private val _data: MutableList<GameDataModel> = mutableListOf()
    private val mutex = Mutex()

    private suspend inline fun <T> data(crossinline block: MutableList<GameDataModel>.() -> T): T {
        mutex.withLock { return _data.block() }
    }
    override suspend fun addGameData(gameData: GameDataModel): Boolean {
        //add data if id is not already in the list or both userid is the same and time is the same
        val success = data { !any { it.id == gameData.id || (it.userId1 == gameData.userId1
                && it.userId2 == gameData.userId2 && it.timeStamp == gameData.timeStamp) } }
        if (success) data { add(gameData) }
        return success
    }

    override suspend fun removeGameData(gameDataId: UUID): Boolean{
        return data {
            find { it.id == gameDataId }?.let {
                if (it.userSaved) {
                    it.userSaved = false
                    true
                } else {
                    false
                }
            } ?: false
        }
    }

    override suspend fun getGameDataById(gameDataId: UUID): GameData {
        val dataModel = data { find { it.id == gameDataId }!! }
        return modelToData(dataModel)
    }

    override suspend fun getGameDataByUser(userId: String): List<GameData> {
        return data { filter { (it.userId1 == userId || it.userId2 == userId) && it.userSaved }.map { modelToData(it) } }
    }

}