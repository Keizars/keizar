package org.keizar.game

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.keizar.utils.communication.game.BoardPos

class RoundSessionTest {
    @Test
    fun `test curPlayer`() = runTest {
        val game = GameSession.create(100)
        val round = game.currentRound.first()
        val curRole: StateFlow<Role> = round.curRole
        assertEquals(Role.WHITE, curRole.value)
        round.move(BoardPos("a2"), BoardPos("a3"))
        assertEquals(Role.BLACK, curRole.value)
        round.move(BoardPos("d7"), BoardPos("d6"))
        assertEquals(Role.WHITE, curRole.value)
    }

    @Test
    fun `test getAvailableTargets`() = runTest {
        val game = GameSession.create(100)
        val round = game.currentRound.first()
        val targets1 = round.getAvailableTargets(BoardPos("d2"))
        val targets2 = round.getAvailableTargets(BoardPos("e2"))

        assertEquals(listOf("d3").map { BoardPos.fromString(it) }.toSet(), targets1.toSet())
        assertEquals(listOf("e3", "e4").map { BoardPos.fromString(it) }.toSet(), targets2.toSet())

        assertTrue(round.move(BoardPos("d2"), BoardPos("d3")))
        val targets3 = round.getAvailableTargets(BoardPos("d3"))
        assertEquals(
            setOf("d2", "d4", "e3", "e4", "c3", "c4").map { BoardPos.fromString(it) }.toSet(),
            targets3.toSet()
        )
    }

    @Test
    fun `test getLostPiecesCount`() = runTest {
        val game = GameSession.create(0)
        val round = game.currentRound.first()
        assertEquals(0, round.getLostPiecesCount(Role.WHITE).value)
        assertEquals(0, round.getLostPiecesCount(Role.BLACK).value)

        assertTrue(round.move(BoardPos("f2"), BoardPos("f4")))
        assertTrue(round.move(BoardPos("e7"), BoardPos("e6")))
        assertTrue(round.move(BoardPos("f4"), BoardPos("f5")))
        assertTrue(round.move(BoardPos("e6"), BoardPos("f5")))

        assertEquals(1, round.getLostPiecesCount(Role.WHITE).value)
        assertEquals(0, round.getLostPiecesCount(Role.BLACK).value)

        assertTrue(round.move(BoardPos("g2"), BoardPos("g4")))
        assertTrue(round.move(BoardPos("g7"), BoardPos("g6")))
        assertTrue(round.move(BoardPos("g4"), BoardPos("f5")))
        assertTrue(round.move(BoardPos("g6"), BoardPos("f5")))

        assertEquals(2, round.getLostPiecesCount(Role.WHITE).value)
        assertEquals(1, round.getLostPiecesCount(Role.BLACK).value)
    }

    @Test
    fun `test winning conditions`() = runTest {
        val game = GameSession.create(0)
        val round = game.currentRound.first()
        assertEquals(null, round.winner.value)
        assertEquals(0, round.winningCounter.value)

        assertTrue(round.move(BoardPos("e2"), BoardPos("e3")))
        assertTrue(round.move(BoardPos("d7"), BoardPos("d6")))
        assertTrue(round.move(BoardPos("e3"), BoardPos("d5")))
        assertTrue(round.move(BoardPos("a7"), BoardPos("a6")))
        assertEquals(null, round.winner.value)
        assertEquals(1, round.winningCounter.value)

        assertTrue(round.move(BoardPos("a2"), BoardPos("a3")))
        assertTrue(round.move(BoardPos("a6"), BoardPos("a5")))
        assertEquals(null, round.winner.value)
        assertEquals(2, round.winningCounter.value)

        assertTrue(round.move(BoardPos("a3"), BoardPos("a4")))
        assertTrue(round.move(BoardPos("d6"), BoardPos("d5")))
        assertEquals(null, round.winner.value)
        assertEquals(0, round.winningCounter.value)

        assertTrue(round.move(BoardPos("h2"), BoardPos("h3")))
        assertTrue(round.move(BoardPos("f7"), BoardPos("f6")))
        assertEquals(null, round.winner.value)
        assertEquals(1, round.winningCounter.value)

        assertTrue(round.move(BoardPos("h3"), BoardPos("h4")))
        assertTrue(round.move(BoardPos("f6"), BoardPos("f5")))
        assertEquals(null, round.winner.value)
        assertEquals(2, round.winningCounter.value)

        assertTrue(round.move(BoardPos("h4"), BoardPos("h5")))
        assertEquals(Role.BLACK, round.winner.value)
        assertEquals(3, round.winningCounter.value)
    }


    @Test
    fun `test getAllPiecesPos`() = runTest {
        val game = GameSession.create(0)
        val round = game.currentRound.first()
        assertEquals(
            BoardPos.range("a1" to "h2").toSet(),
            round.getAllPiecesPos(Role.WHITE).toSet(),
        )
        assertEquals(
            BoardPos.range("a7" to "h8").toSet(),
            round.getAllPiecesPos(Role.BLACK).toSet(),
        )

        assertTrue(round.move(BoardPos("f2"), BoardPos("f4")))
        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "f4", "g2", "h2",
            ).map { BoardPos.fromString(it) }.toSet(),
            round.getAllPiecesPos(Role.WHITE).toSet(),
        )

        assertTrue(round.move(BoardPos("e7"), BoardPos("e6")))
        assertTrue(round.move(BoardPos("f4"), BoardPos("f5")))
        assertTrue(round.move(BoardPos("e6"), BoardPos("f5")))

        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "g2", "h2",
            ).map { BoardPos.fromString(it) }.toSet(),
            round.getAllPiecesPos(Role.WHITE).toSet(),
        )

        assertEquals(
            setOf(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "f7", "g7", "h7", "f5",
            ).map { BoardPos.fromString(it) }.toSet(),
            round.getAllPiecesPos(Role.BLACK).toSet(),
        )
    }

    @Test
    fun `test pieces`() = runTest {
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

        assertTrue(round.move(BoardPos("e7"), BoardPos("e6")))
        assertTrue(round.move(BoardPos("f4"), BoardPos("f5")))
        assertTrue(round.move(BoardPos("e6"), BoardPos("f5")))

        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "g2", "h2",
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.WHITE && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertEquals(
            setOf(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "f7", "g7", "h7", "f5",
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.BLACK && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )
    }

    @Test
    fun `test undo`() = runTest {
        val game = GameSession.create(0)
        val round = game.currentRound.first()
        val pieces = round.pieces

        assertFalse(round.undo(Role.WHITE))

        assertTrue(round.move(BoardPos("f2"), BoardPos("f4")))
        assertFalse(round.undo(Role.WHITE))

        assertTrue(round.move(BoardPos("e7"), BoardPos("e6")))
        assertTrue(round.move(BoardPos("f4"), BoardPos("f5")))
        assertFalse(round.undo(Role.WHITE))

        assertTrue(round.move(BoardPos("e6"), BoardPos("f5")))
        assertTrue(round.undo(Role.WHITE))

        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "f4", "g2", "h2"
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.WHITE && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertEquals(
            setOf(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e6", "f7", "g7", "h7",
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.BLACK && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )
    }

    @Test
    fun `test redo`() = runTest {
        val game = GameSession.create(0)
        val round = game.currentRound.first()
        val pieces = round.pieces

        assertFalse(round.undo(Role.WHITE))
        assertFalse(round.redo(Role.WHITE))

        assertTrue(round.move(BoardPos("f2"), BoardPos("f4")))
        assertFalse(round.undo(Role.WHITE))

        assertTrue(round.move(BoardPos("e7"), BoardPos("e6")))
        assertTrue(round.move(BoardPos("f4"), BoardPos("f5")))
        assertFalse(round.undo(Role.WHITE))

        assertTrue(round.move(BoardPos("e6"), BoardPos("f5")))
        assertTrue(round.undo(Role.WHITE))
        assertTrue(round.redo(Role.WHITE))

        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "g2", "h2"
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.WHITE && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertEquals(
            setOf(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "f5", "f7", "g7", "h7",
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.BLACK && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertTrue(round.move(BoardPos("a2"), BoardPos("a3")))
        assertTrue(round.undo(Role.BLACK))
        assertTrue(round.undo(Role.BLACK))
        assertTrue(round.redo(Role.BLACK))

        assertTrue(round.move(BoardPos("e6"), BoardPos("f5")))
        assertFalse(round.redo(Role.WHITE))
    }

    @Test
    fun `test revertLast and recoverLast`() = runTest {
        val game = GameSession.create(0)
        val round = game.currentRound.first()
        val pieces = round.pieces

        assertFalse(round.revertLast())

        assertTrue(round.move(BoardPos("f2"), BoardPos("f4")))
        assertTrue(round.move(BoardPos("e7"), BoardPos("e6")))
        assertTrue(round.move(BoardPos("f4"), BoardPos("f5")))
        assertTrue(round.revertLast())
        assertTrue(round.move(BoardPos("f4"), BoardPos("f5")))
        assertTrue(round.move(BoardPos("e6"), BoardPos("f5")))

        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "g2", "h2"
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.WHITE && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertEquals(
            setOf(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "f5", "f7", "g7", "h7",
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.BLACK && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertTrue(round.move(BoardPos("a2"), BoardPos("a3")))
        assertTrue(round.revertLast())
        assertTrue(round.revertLast())
        assertTrue(round.recoverLast())

        assertEquals(
            setOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "g2", "h2"
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.WHITE && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertEquals(
            setOf(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "f5", "f7", "g7", "h7",
            ).map { BoardPos.fromString(it) }.toSet(),
            pieces.filter { it.role == Role.BLACK && !it.isCaptured.value }.map { it.pos.value }
                .toSet(),
        )

        assertTrue(round.move(BoardPos("h2"), BoardPos("h3")))
        assertFalse(round.recoverLast())
    }

    @Test
    fun `test getNeutralStatistics`() = runTest {
        val game = GameSession.create(0)
        val round = game.currentRound.first()

        assertTrue(round.move(BoardPos("f2"), BoardPos("f4")))
        assertTrue(round.move(BoardPos("e7"), BoardPos("e6")))
        assertTrue(round.move(BoardPos("f4"), BoardPos("f5")))
        assertTrue(round.move(BoardPos("e6"), BoardPos("f5")))
        assertTrue(round.move(BoardPos("g2"), BoardPos("g4")))
        assertTrue(round.move(BoardPos("g7"), BoardPos("g6")))
        assertTrue(round.move(BoardPos("g4"), BoardPos("f5")))
        assertTrue(round.move(BoardPos("g6"), BoardPos("f5")))

        val stats = round.getNeutralStatistics()
        assertEquals(1, stats.whiteCaptured)
        assertEquals(2, stats.blackCaptured)
        assertEquals(4, stats.whiteMoves)
        assertEquals(4, stats.blackMoves)
    }
}