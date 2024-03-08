package org.keizar.server.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.server.data.GameDataRepository
import org.keizar.server.data.models.GameDataModel
import java.util.UUID

class InMemoryGameDataRepository: GameDataRepository {
    private val _data: MutableList<GameDataModel> = mutableListOf()
    private val mutex = Mutex()

    private suspend inline fun <T> data(crossinline block: MutableList<GameDataModel>.() -> T): T {
        mutex.withLock { return _data.block() }
    }
    override suspend fun addGameData(gameData: GameDataModel): Boolean {
        //add data if id is not already in the list or both userid is the same and time is the same
        val success = data { !any { it.id == gameData.id || (it.userId == gameData.userId
                && it.opponentId == gameData.opponentId && it.timeStamp == gameData.timeStamp) } }
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

    override suspend fun getGameDataById(gameDataId: UUID): GameDataModel {
        return data { find { it.id == gameDataId }!! }
    }

    override suspend fun getGameDataByUser(userId: String): List<GameDataModel> {
        return data { filter { (it.userId == userId) && it.userSaved }.map { it } }
    }

    override suspend fun saveGameData(dataId: UUID): Boolean {
        data { find { it.id == dataId }?.let { it.userSaved = true } }
        return data { any { it.id == dataId && it.userSaved }}
    }

}