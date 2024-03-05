package org.keizar.server.database.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.server.database.SeedBankDbControl
import org.keizar.server.database.models.SeedBankModel

class InMemorySeedBankDbControl : SeedBankDbControl {
    private val _data: MutableList<SeedBankModel> = mutableListOf()
    private val mutex = Mutex()
    private suspend inline fun <T> data(crossinline block: MutableList<SeedBankModel>.() -> T): T {
        mutex.withLock { return _data.block() }
    }

    override suspend fun addSeed(userId: String, seed: String): Boolean {
        return data {
            val seedBankModel = SeedBankModel(userId = userId, gameSeed = seed)
            if (any { it.userId == userId && it.gameSeed == seed }) return@data false
            add(seedBankModel)
            return@data true
        }
    }

    override suspend fun removeSeed(userId: String, seed: String): Boolean {
        return data { removeIf { it.userId == userId && it.gameSeed == seed } }
    }

    override suspend fun getSeeds(userId: String): List<String> {
        return data { filter { it.userId == userId }.map { it.gameSeed } }
    }
}