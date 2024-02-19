package org.keizar.aiengine

import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.keizar.game.GameSession
import org.keizar.game.snapshot.buildGameSnapshot
import org.keizar.utils.communication.game.GameResult
import org.keizar.utils.communication.game.Player
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds


class RoundSessionTest {
    @Test
    fun `test AI move and play 2 round and Draw`() = runTest {
        val gameSnapshot = buildGameSnapshot {
            val curRound = round {
                resetPieces {
                    white("a7")
                    black("a1")
                }
            }
            round {
                resetPieces {
                    white("a7")
                    black("a1")
                }
            }
            setCurRound(curRound)
        }

        val game = GameSession.restore(gameSnapshot)
        val context = EmptyCoroutineContext
        val ai1 = RandomGameAIImpl(game, Player.FirstWhitePlayer, context, true)
        val ai2 = RandomGameAIImpl(game, Player.FirstBlackPlayer, context, true)
        assertEquals(0, game.currentRoundNo.value)
        ai1.start()
        ai2.start()
        game.finalWinner.filterNotNull().take(2).collectIndexed() { index, winner ->
            if (index == 1) {
                assertEquals(GameResult.Draw, winner)
            }
        }
    }

//    @Test
//    fun `test remote AI combat`() = runTest {
//        runServer {
//            val context = EmptyCoroutineContext
//            val gameClient1 = RemoteGameSession.create(
//                roomNumber = 5462u,
//                parentCoroutineContext = EmptyCoroutineContext,
//                seed = 100
//            )
//            val gameClient2 = RemoteGameSession.create(
//                roomNumber = 5462u,
//                parentCoroutineContext = EmptyCoroutineContext,
//                seed = 100
//            )
//            gameClient1.waitUntilOpponentFound()
//            gameClient2.waitUntilOpponentFound()
//
//            val ai1 = RandomGameAIImpl(gameClient1, Player.FirstWhitePlayer, context, true)
//            val ai2 = RandomGameAIImpl(gameClient2, Player.FirstBlackPlayer, context, true)
//            ai1.start()
//            ai2.start()
//            val winner = gameClient1.finalWinner.filterNotNull().first()
//            assertNotNull(winner)
//            println(winner)
//        }
//    }

//    @Test
//    fun `Test q_table combat`() = runTest {
//        val game = GameSession.create(0)
//        val context = EmptyCoroutineContext
//        val ai1 = QTableAI(game, Player.FirstWhitePlayer, context, true)
//        val ai2 = RandomGameAIImpl(game, Player.FirstBlackPlayer, context, true)
//        ai1.start()
//        ai2.start()
//        val winner = game.finalWinner.filterNotNull().first()
//        assertNotNull(winner)
//        println(winner)
//    }
}