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


class HyperParameterTuning {
    fun `better parameter tuning`() = runTest(timeout = 1000.seconds) {
        var ai1Win = 0
        var ai2Win = 0
        for (i in 1..500) {
            println("Session: $i ")
            val game = GameSession.create(0)
            val context = EmptyCoroutineContext
            val ai1Parameter = AIParameters(
                keizarThreshold = 1,
                possibleMovesThreshold = 5,
                noveltyLevel = 0.9
            )
            val ai2Parameter = AIParameters(
                keizarThreshold = 2,
                possibleMovesThreshold = 5,
                noveltyLevel = 0.9
            )
            val ai1 = AlgorithmAI(game, Player.FirstWhitePlayer, context, true, ai1Parameter)
            val ai2 = AlgorithmAI(game, Player.FirstBlackPlayer, context, true, ai2Parameter)
            ai1.start()
            ai2.start()
            val winner = game.finalWinner.filterNotNull().first()
            if (winner == GameResult.Winner(Player.FirstWhitePlayer)) {
                ai1Win++
            } else if (winner == GameResult.Winner(Player.FirstBlackPlayer)) {
                ai2Win++
            }
            ai1.end()
            ai2.end()
        }
        println("ai1Win: $ai1Win")
        println("ai2Win: $ai2Win")
    }
}

fun main() {
    val hyperParameterTuning = HyperParameterTuning()
    hyperParameterTuning.`better parameter tuning`()
}