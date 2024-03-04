package org.keizar.server

import kotlinx.coroutines.test.runTest
import org.keizar.server.database.InMemoryDatabaseManagerImpl
import org.keizar.server.modules.GameDataModuleImpl
import org.keizar.utils.communication.game.GameDataGet
import org.keizar.utils.communication.game.NeutralStats
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.game.RoundStats
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameDataModuleTest {
    @Test
    fun `test addData and getData`() = runTest {
        val gameDataModule = GameDataModuleImpl(database = InMemoryDatabaseManagerImpl())
        val round1NeutralStats = NeutralStats(1, 2, 3.0, 4, 5, 6.0, 7, 8)
        val round2NeutralStats = NeutralStats(9, 10, 11.0, 12, 13, 14.0, 15, 16)
        val round1Stats = RoundStats(round1NeutralStats, Player.FirstBlackPlayer, Player.FirstBlackPlayer)
        val round2Stats = RoundStats(round2NeutralStats, Player.FirstBlackPlayer, Player.FirstBlackPlayer)
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val userId3 = UUID.randomUUID()
        val gameData1 = org.keizar.utils.communication.game.GameDataStore(
            id = null,
            round1Stats,
            round2Stats,
            "gameConfiguration",
            userId1.toString(),
            userId2.toString(),
            "timeStamp",
            false
        )
        val gameData2 = org.keizar.utils.communication.game.GameDataStore(
            id = null,
            round1Stats,
            round2Stats,
            "gameConfiguration",
            userId1.toString(),
            userId3.toString(),
            "timeStamp",
            true
        )
        // add a non user saved game data
        assertTrue(gameDataModule.addGameData(gameData1))
        assertFalse(gameDataModule.addGameData(gameData1))
        // shouldn't be able to get it
        assertFalse(gameDataModule.getGameDataByUsedID(userId1).isNotEmpty())
        // add a user saved game data
        assertTrue(gameDataModule.addGameData(gameData2))
        val gameData = gameDataModule.getGameDataByUsedID(userId1)
        val expectedData = GameDataGet (
            "Unknown",
            "Unknown",
            "timeStamp",
            "gameConfiguration",
            round1Stats,
            round2Stats,
            gameData[0].dataId
        )
        assertContentEquals(gameData, listOf(expectedData))
    }

    @Test
    fun `test remove data`() = runTest{
        val gameDataModule = GameDataModuleImpl(database = InMemoryDatabaseManagerImpl())
        val round1NeutralStats = NeutralStats(1, 2, 3.0, 4, 5, 6.0, 7, 8)
        val round2NeutralStats = NeutralStats(9, 10, 11.0, 12, 13, 14.0, 15, 16)
        val round1Stats = RoundStats(round1NeutralStats, Player.FirstBlackPlayer, Player.FirstBlackPlayer)
        val round2Stats = RoundStats(round2NeutralStats, Player.FirstBlackPlayer, Player.FirstBlackPlayer)
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val uuidString = UUID.randomUUID().toString()
        val gameData1 = org.keizar.utils.communication.game.GameDataStore(
            id = uuidString,
            round1Stats,
            round2Stats,
            "gameConfiguration",
            userId1.toString(),
            userId2.toString(),
            "timeStamp",
            true
        )
        assertTrue (gameDataModule.addGameData(gameData1))
        assertTrue (gameDataModule.removeGameData(UUID.fromString(uuidString)))
        assertFalse (gameDataModule.removeGameData(UUID.fromString(uuidString)))
        assertFalse { gameDataModule.getGameDataByUsedID(userId1).isNotEmpty() }
    }
}