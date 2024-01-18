package org.keizar.game

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class GameSessionTest {
    @Test
    fun `test curPlayer`() = runTest {
        val game = GameSession.create(Random(100))
        val curPlayer: StateFlow<Player> = game.curPlayer
        assertEquals(Player.WHITE, curPlayer.value)
        game.move(BoardPos("a2"), BoardPos("a3"))
        assertEquals(Player.BLACK, curPlayer.value)
        game.move(BoardPos("d7"), BoardPos("d6"))
        assertEquals(Player.WHITE, curPlayer.value)
    }

    @Test
    fun `test getAvailableTargets`() = runTest {
        val game = GameSession.create(Random(100))
        val targets1 = game.getAvailableTargets(BoardPos("d2"))
        val targets2 = game.getAvailableTargets(BoardPos("e2"))

        targets1.collect { list ->
            assertEquals(listOf("d3").map { BoardPos.fromString(it) }.toSet(), list.toSet())
        }
        targets2.collect { list ->
            assertEquals(listOf("e3", "e4").map { BoardPos.fromString(it) }.toSet(), list.toSet())
        }

        assertTrue(game.move(BoardPos("d2"), BoardPos("d3")))
        val targets3 = game.getAvailableTargets(BoardPos("d3"))
        targets3.collect { list ->
            assertEquals(
                setOf("d2", "d4", "e3", "e4", "c3", "c4").map { BoardPos.fromString(it) }.toSet(),
                list.toSet()
            )
        }
    }
}