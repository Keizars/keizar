package org.keizar.game

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GameSessionTest {
    @Test
    fun `test currentRole`() = runTest {
        val game = GameSession.create(0)
        assertEquals(Role.WHITE, game.currentRole(Player.Player1).value)
        assertEquals(Role.BLACK, game.currentRole(Player.Player2).value)
    }

    @Test
    fun `test currentRole, currentRoundNo and confirmNextRound`() = runTest {
        val game = GameSession.create(0)
        assertEquals(0, game.currentRoundNo.value)
        assertEquals(Role.WHITE, game.currentRole(Player.Player1).value)
        assertEquals(Role.BLACK, game.currentRole(Player.Player2).value)
        assertTrue(game.confirmNextRound(Player.Player1))
        assertTrue(game.confirmNextRound(Player.Player2))

        assertEquals(1, game.currentRoundNo.value)
        assertEquals(Role.BLACK, game.currentRole(Player.Player1).value)
        assertEquals(Role.WHITE, game.currentRole(Player.Player2).value)
    }

    @Test
    fun `test confirmNextRound and finalWinner`() = runTest {
        val game = GameSession.create(0)
        assertTrue(game.confirmNextRound(Player.Player1))
        assertTrue(game.confirmNextRound(Player.Player2))
        assertTrue(game.confirmNextRound(Player.Player1))
        assertTrue(game.confirmNextRound(Player.Player2))
        assertEquals(2, game.currentRoundNo.value)
        assertEquals(GameResult.Draw, game.finalWinner.first())
        assertFalse(game.confirmNextRound(Player.Player1))
        assertFalse(game.confirmNextRound(Player.Player2))
    }

    @Test
    fun `test confirmNextRound and winningCounter`() = runTest {
        val game = GameSession.create(0)
        val curRound = game.currentRound
        val round1 = curRound.first()
        assertEquals(null, round1.winner.value)
        assertEquals(0, round1.winningCounter.value)

        assertTrue(round1.move(BoardPos("e2"), BoardPos("e3")))
        assertTrue(round1.move(BoardPos("d7"), BoardPos("d6")))
        assertTrue(round1.move(BoardPos("e3"), BoardPos("d5")))
        assertTrue(round1.move(BoardPos("a7"), BoardPos("a6")))
        assertTrue(round1.move(BoardPos("a2"), BoardPos("a3")))
        assertTrue(round1.move(BoardPos("a6"), BoardPos("a5")))
        assertTrue(round1.move(BoardPos("a3"), BoardPos("a4")))
        assertTrue(round1.move(BoardPos("d6"), BoardPos("d5")))
        assertTrue(round1.move(BoardPos("h2"), BoardPos("h3")))
        assertTrue(round1.move(BoardPos("f7"), BoardPos("f6")))
        assertTrue(round1.move(BoardPos("h3"), BoardPos("h4")))
        assertTrue(round1.move(BoardPos("f6"), BoardPos("f5")))
        assertTrue(round1.move(BoardPos("h4"), BoardPos("h5")))
        assertEquals(Role.BLACK, round1.winner.value)
        assertEquals(3, round1.winningCounter.value)

        assertTrue(game.confirmNextRound(Player.Player1))
        assertTrue(game.confirmNextRound(Player.Player2))

        val round2 = curRound.first()

        assertEquals(1, game.currentRoundNo.value)
        assertEquals(0, round2.winningCounter.value)
        assertEquals(0, game.wonRounds(Player.Player1).value)
        assertEquals(1, game.wonRounds(Player.Player2).value)
        assertEquals(null, game.finalWinner.first())

        assertTrue(game.confirmNextRound(Player.Player1))
        assertTrue(game.confirmNextRound(Player.Player2))
        assertEquals(GameResult.Winner(Player.Player2), game.finalWinner.first())
    }

    @Test
    fun `test getSnapshot and restore`() = runTest {
        val game = GameSession.create(0)
        val round = game.currentRound.first()
        val pieces = round.pieces
        assertEquals(
            BoardPos.range("a1" to "h2").toSet() + BoardPos.range("a7" to "h8").toSet(),
            pieces.map { it.pos.value }.toSet()
        )

        assertTrue(round.move(BoardPos("f2"), BoardPos("f4")))
        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "f4", "g2", "h2",
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.WHITE }.map { it.pos.value }.toSet(),
        )

        val snapshot = game.getSnapshot()
        val newGame = GameSession.restore(snapshot)
        val newRound = game.currentRound.first()
        val newPieces = newRound.pieces
        assertEquals(snapshot, newGame.getSnapshot())

        assertTrue(newRound.move(BoardPos("e7"), BoardPos("e5")))
        assertTrue(newRound.move(BoardPos("f4"), BoardPos("e5")))

        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "g2", "h2", "e5"
            ).map { BoardPos.fromString(it) }.toSet(),
            newPieces.filter { it.role == Role.WHITE && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertEquals(
            setOf(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "f7", "g7", "h7",
            ).map { BoardPos.fromString(it) }.toSet(),
            newPieces.filter { it.role == Role.BLACK && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertEquals(0, newRound.getLostPiecesCount(Role.WHITE).value)
        assertEquals(1, newRound.getLostPiecesCount(Role.BLACK).value)

        assertTrue(newRound.move(BoardPos("d7"), BoardPos("d6")))
        assertTrue(newRound.move(BoardPos("e5"), BoardPos("d6")))
        assertTrue(newRound.move(BoardPos("c7"), BoardPos("d6")))

        assertEquals(1, newRound.getLostPiecesCount(Role.WHITE).value)
        assertEquals(2, newRound.getLostPiecesCount(Role.BLACK).value)
    }
}