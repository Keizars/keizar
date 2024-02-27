package org.keizar.server.modules

import org.keizar.server.ServerContext
import java.util.UUID

interface SeedBankModule {
    fun getSeeds(userId: UUID): List<String>
    fun addSeed(userId: UUID, seed: String): Boolean
    fun removeSeed(userId: UUID, seed: String): Boolean
}

class SeedBankModuleImpl() : SeedBankModule {
    override fun getSeeds(userId: UUID): List<String> {
        TODO("Not yet implemented")
    }

    override fun addSeed(userId: UUID, seed: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeSeed(userId: UUID, seed: String): Boolean {
        TODO("Not yet implemented")
    }
}
