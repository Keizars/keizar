package org.keizar.server.database

interface SeedBankDbControl {
    suspend fun addSeed(userId: String, seed: String): Boolean
    suspend fun removeSeed(userId: String, seed: String): Boolean
    suspend fun getSeeds(userId: String): List<String>
}