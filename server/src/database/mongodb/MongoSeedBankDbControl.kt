package org.keizar.server.database.mongodb

import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.keizar.server.database.SeedBankDbControl
import org.keizar.server.database.models.SeedBankModel

class MongoSeedBankDbControl(
    private val seedBankTable: MongoCollection<SeedBankModel>
) : SeedBankDbControl {
    override suspend fun addSeed(userId: String, seed: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun removeSeed(userId: String, seed: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getSeeds(userId: String): List<String> {
        TODO("Not yet implemented")
    }
}