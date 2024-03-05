package org.keizar.server.modules

import org.keizar.server.ServerContext
import org.keizar.server.database.DatabaseManager
import java.util.UUID

interface SeedBankModule {
    suspend fun getSeeds(userId: UUID): List<String>
    suspend fun addSeed(userId: UUID, seed: String): Boolean
    suspend fun removeSeed(userId: UUID, seed: String): Boolean
}

class SeedBankModuleImpl(
    private val database: DatabaseManager,
) : SeedBankModule {
    override suspend fun getSeeds(userId: UUID): List<String> {
        return database.seedBank.getSeeds(userId.toString())
    }

    override suspend fun addSeed(userId: UUID, seed: String): Boolean {
        if (database.seedBank.getSeeds(userId.toString()).contains(seed)) {
            return true
        }
        return database.seedBank.addSeed(userId.toString(), seed)
    }

    override suspend fun removeSeed(userId: UUID, seed: String): Boolean {
        return database.seedBank.removeSeed(userId.toString(), seed)
    }
}
