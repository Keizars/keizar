import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.keizar.game.BoardPropertiesPrototypes
import org.keizar.game.snapshot.buildGameSession
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.GameResult
import org.keizar.utils.communication.game.Player

class GameSnapshotBuilderTest {
    @Test
    fun `test GameSnapshotBuilder default behaviour`() = runTest {
        val game = buildGameSession(prototype = BoardPropertiesPrototypes.Standard(0)) { }
        val round1 = game.currentRound.first()
        assertTrue(round1.move(BoardPos("e2"), BoardPos("e3")))
        assertTrue(round1.move(BoardPos("d7"), BoardPos("d6")))
        assertTrue(round1.move(BoardPos("e3"), BoardPos("d5")))
        assertTrue(round1.move(BoardPos("a7"), BoardPos("a6")))
        assertTrue(round1.move(BoardPos("a2"), BoardPos("a3")))
        assertTrue(round1.move(BoardPos("a6"), BoardPos("a5")))
        assertTrue(round1.move(BoardPos("a3"), BoardPos("a4")))
        assertTrue(round1.move(BoardPos("h7"), BoardPos("h6")))
        assertTrue(game.confirmNextRound(Player.FirstWhitePlayer))
        assertTrue(game.confirmNextRound(Player.FirstBlackPlayer))

        val round2 = game.currentRound.first()
        assertTrue(round2.move(BoardPos("e2"), BoardPos("e3")))
        assertTrue(round2.move(BoardPos("d7"), BoardPos("d6")))
        assertTrue(round2.move(BoardPos("e3"), BoardPos("d5")))
        assertTrue(round2.move(BoardPos("a7"), BoardPos("a6")))
        assertTrue(round2.move(BoardPos("a2"), BoardPos("a3")))
        assertTrue(round2.move(BoardPos("a6"), BoardPos("a5")))
        assertTrue(round2.move(BoardPos("a3"), BoardPos("a4")))
        assertTrue(round2.move(BoardPos("h7"), BoardPos("h6")))
        assertTrue(game.confirmNextRound(Player.FirstWhitePlayer))
        assertTrue(game.confirmNextRound(Player.FirstBlackPlayer))
        assertEquals(1, game.currentRoundNo.value)
        assertEquals(GameResult.Draw, game.finalWinner.first())
    }

    @Test
    fun `test free move round builder`() = runTest {
        val game = buildGameSession {
            tiles {  }
            round {
                allowFreeMove()
                resetPieces {
                    white("b1")
                    black("g7")
                }
            }
        }
        val round = game.currentRound.first()
        assertTrue(round.move(BoardPos("g7"), BoardPos("g5")))
        assertTrue(round.move(BoardPos("g5"), BoardPos("g4")))
        assertTrue(round.move(BoardPos("g4"), BoardPos("g3")))
    }

    @Test
    fun `test disable winner round builder`() = runTest {
        val game = buildGameSession {
            tiles {  }
            round {
                disableWinner()
                resetPieces {
                    white("c1")
                    black("g7")
                    black("d5")
                }
            }
        }
        val round = game.currentRound.first()
        assertTrue(round.move(BoardPos("c1"), BoardPos("c2")))
        assertTrue(round.move(BoardPos("g7"), BoardPos("g6")))
        assertNull(round.winner.value)
        assertTrue(round.move(BoardPos("c2"), BoardPos("c3")))
        assertTrue(round.move(BoardPos("g6"), BoardPos("g5")))
        assertNull(round.winner.value)
        assertTrue(round.move(BoardPos("c3"), BoardPos("c4")))
        assertTrue(round.move(BoardPos("g5"), BoardPos("g4")))
        assertNull(round.winner.value)
        assertTrue(round.move(BoardPos("c4"), BoardPos("d5")))
        assertNull(round.winner.value)
    }
}
