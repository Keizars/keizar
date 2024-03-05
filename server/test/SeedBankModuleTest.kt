package org.keizar.server

import kotlinx.coroutines.test.runTest
import org.keizar.server.database.InMemoryDatabaseManagerImpl
import org.keizar.server.modules.SeedBankModuleImpl
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeedBankModuleTest {
    @Test
    fun `test addSeed and getSeeds`() = runTest {
        val seedBankModule = SeedBankModuleImpl(database = InMemoryDatabaseManagerImpl())
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        assertTrue(seedBankModule.addSeed(userId1, "seed1"))
        assertTrue(seedBankModule.addSeed(userId1, "seed1"))

        assertTrue(seedBankModule.addSeed(userId2, "seed2"))
        assertTrue(seedBankModule.addSeed(userId1, "seed3"))
        assertTrue(seedBankModule.addSeed(userId2, "seed4"))
        assertTrue(seedBankModule.addSeed(userId1, "seed5"))
        val seeds = seedBankModule.getSeeds(userId1)
        assertContentEquals(seeds, listOf("seed1", "seed3", "seed5"))
    }

    @Test
    fun `test removeSeed`() = runTest {
        val seedBankModule = SeedBankModuleImpl(database = InMemoryDatabaseManagerImpl())
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        assertTrue(seedBankModule.addSeed(userId1, "seed1"))
        assertTrue(seedBankModule.addSeed(userId2, "seed2"))
        assertTrue(seedBankModule.addSeed(userId1, "seed3"))
        assertTrue(seedBankModule.addSeed(userId2, "seed4"))
        assertTrue(seedBankModule.addSeed(userId1, "seed5"))
        assertTrue(seedBankModule.removeSeed(userId1, "seed1"))
        assertTrue(seedBankModule.removeSeed(userId2, "seed2"))
        assertTrue(seedBankModule.removeSeed(userId2, "seed4"))
        assertContentEquals( seedBankModule.getSeeds(userId1), listOf("seed3", "seed5"))
        assertContentEquals( seedBankModule.getSeeds(userId2), listOf())
    }
}