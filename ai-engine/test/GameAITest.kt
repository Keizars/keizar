package org.keizar.aiengine

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.Test
import org.keizar.game.GameSession
import org.keizar.utils.communication.game.Player
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.assertNotNull


class RoundSessionTest {
    @Test
    fun `test generate a AI`() = runTest {
        val game = GameSession.create(100)
        val context = EmptyCoroutineContext
        val ai1 = RandomGameAIImpl(game, Player.FirstWhitePlayer, context, true)
        val ai2 = RandomGameAIImpl(game, Player.FirstBlackPlayer, context, true)
        ai1.start()
        ai2.start()
        val winner = game.finalWinner.filterNotNull().first()
        assertNotNull(winner)
        println(winner)
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
}
