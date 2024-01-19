package org.keizar.game

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toSet
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

    @Test
    fun `test winning conditions`() = runTest {
        val game = GameSession.create(Random(0))
        assertEquals(null, game.winner.value)
        assertEquals(0, game.winningCounter.value)

        assertTrue(game.move(BoardPos("e2"), BoardPos("e3")))
        assertTrue(game.move(BoardPos("d7"), BoardPos("d6")))
        assertTrue(game.move(BoardPos("e3"), BoardPos("d5")))
        assertTrue(game.move(BoardPos("a7"), BoardPos("a6")))
        assertEquals(null, game.winner.value)
        //assertEquals(1, game.winningCounter.value)

        assertTrue(game.move(BoardPos("a2"), BoardPos("a3")))
        assertTrue(game.move(BoardPos("a6"), BoardPos("a5")))
        assertEquals(null, game.winner.value)
        //assertEquals(2, game.winningCounter.value)

        assertTrue(game.move(BoardPos("a3"), BoardPos("a4")))
        assertTrue(game.move(BoardPos("d6"), BoardPos("d5")))
        assertEquals(null, game.winner.value)
        //assertEquals(0, game.winningCounter.value)

        assertTrue(game.move(BoardPos("h2"), BoardPos("h3")))
        assertTrue(game.move(BoardPos("f7"), BoardPos("f6")))
        assertEquals(null, game.winner.value)
        //assertEquals(1, game.winningCounter.value)

        assertTrue(game.move(BoardPos("h3"), BoardPos("h4")))
        assertTrue(game.move(BoardPos("f6"), BoardPos("f5")))
        assertEquals(null, game.winner.value)
        //assertEquals(2, game.winningCounter.value)

        assertTrue(game.move(BoardPos("h4"), BoardPos("h5")))
        assertEquals(Player.BLACK, game.winner.value)
        //assertEquals(3, game.winningCounter.value)
    }


    @Test
    fun `test getAllPiecesPos`() = runTest {
        val game = GameSession.create(Random(0))
        assertEquals(
            BoardPos.range("a1" to "h2").toSet(),
            game.getAllPiecesPos(Player.WHITE).last().toSet(),
        )
        assertEquals(
            BoardPos.range("a7" to "h8").toSet(),
            game.getAllPiecesPos(Player.BLACK).last().toSet(),
        )

        assertTrue(game.move(BoardPos("f2"), BoardPos("f4")))
        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "f4", "g2", "h2",
            ).map { BoardPos.fromString(it) }.toSet(),
            game.getAllPiecesPos(Player.WHITE).last().toSet(),
        )

        assertTrue(game.move(BoardPos("e7"), BoardPos("e5")))
        assertTrue(game.move(BoardPos("f4"), BoardPos("e5")))

        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "g2", "h2", "e5"
            ).map { BoardPos.fromString(it) }.toSet(),
            game.getAllPiecesPos(Player.WHITE).last().toSet(),
        )

        assertEquals(
            setOf(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "f7", "g7", "h7",
            ).map { BoardPos.fromString(it) }.toSet(),
            game.getAllPiecesPos(Player.BLACK).last().toSet(),
        )
    }

    @Test
    fun `test pieces`() = runTest {
        val game = GameSession.create(Random(0))
        val pieces = game.pieces
        assertEquals(
            BoardPos.range("a1" to "h2").toSet() + BoardPos.range("a7" to "h8").toSet(),
            pieces.map { it.pos.value }.toSet()
        )

        assertTrue(game.move(BoardPos("f2"), BoardPos("f4")))
        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "f4", "g2", "h2",
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.player == Player.WHITE }.map { it.pos.value }.toSet(),
        )

        assertTrue(game.move(BoardPos("e7"), BoardPos("e5")))
        assertTrue(game.move(BoardPos("f4"), BoardPos("e5")))

        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "g2", "h2", "e5"
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.player == Player.WHITE && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertEquals(
            setOf(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "f7", "g7", "h7",
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.player == Player.BLACK && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )
    }
}