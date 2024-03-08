package org.keizar.server.data.mongodb

import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.keizar.server.data.SeedBankRepository
import org.keizar.server.data.models.SeedBankModel

class MongoSeedBankRepository(
    private val seedBankTable: MongoCollection<SeedBankModel>
) : SeedBankRepository {
    override suspend fun addSeed(userId: String, seed: String): Boolean {
        return seedBankTable.insertOne(SeedBankModel(userId = userId, gameSeed = seed)).wasAcknowledged()
    }

    override suspend fun removeSeed(userId: String, seed: String): Boolean {
        return seedBankTable.deleteOne(
            filter = (Field("userId") eq userId) and (Field("gameSeed") eq seed)
        ).wasAcknowledged()
    }

    override suspend fun getSeeds(userId: String): List<String> {
        val list = mutableListOf<String>()
        seedBankTable.find(
            filter = Field("userId") eq userId
        ).map { it.gameSeed }.toList(list)
        return list
    }
}