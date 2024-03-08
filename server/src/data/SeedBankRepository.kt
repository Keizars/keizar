package org.keizar.server.data

interface SeedBankRepository {
    suspend fun addSeed(userId: String, seed: String): Boolean
    suspend fun removeSeed(userId: String, seed: String): Boolean
    suspend fun getSeeds(userId: String): List<String>
}