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

    @Test
    fun `test getLostPiecesCount`() = runTest {
        val game = GameSession.create(Random(0))
        assertEquals(0, game.getLostPiecesCount(Player.WHITE).value)
        assertEquals(0, game.getLostPiecesCount(Player.BLACK).value)

        assertTrue(game.move(BoardPos("f2"), BoardPos("f4")))
        assertTrue(game.move(BoardPos("e7"), BoardPos("e5")))
        assertTrue(game.move(BoardPos("f4"), BoardPos("e5")))

        assertEquals(0, game.getLostPiecesCount(Player.WHITE).value)
        assertEquals(1, game.getLostPiecesCount(Player.BLACK).value)

        assertTrue(game.move(BoardPos("d7"), BoardPos("d6")))
        assertTrue(game.move(BoardPos("e5"), BoardPos("d6")))
        assertTrue(game.move(BoardPos("c7"), BoardPos("d6")))

        assertEquals(1, game.getLostPiecesCount(Player.WHITE).value)
        assertEquals(2, game.getLostPiecesCount(Player.BLACK).value)
    }
}