package org.keizar.server.database.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.server.database.models.SeedBankModel
import org.keizar.server.database.SeedBankDbControl

class InMemorySeedBankDbControl : SeedBankDbControl {
    private val _data: MutableList<SeedBankModel> = mutableListOf()
    private val mutex = Mutex()
    private suspend inline fun <T> data(crossinline block: MutableList<SeedBankModel>.() -> T): T {
        mutex.withLock { return _data.block() }
    }

    override suspend fun addSeed(userId: String, seed: String): Boolean {
        return data {
            val seedBankModel = SeedBankModel(userId, seed)
            if (contains(seedBankModel)) return@data false
            add(seedBankModel)
            return@data true
        }
    }

    override suspend fun removeSeed(userId: String, seed: String): Boolean {
        return data { remove(SeedBankModel(userId, seed)) }
    }

    override suspend fun getSeeds(userId: String): List<String> {
        return data { filter { it.userId == userId }.map { it.gameSeed } }
    }
}