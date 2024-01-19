package org.keizar.aiengine

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.random.Random
import org.keizar.game.GameSession
import org.keizar.game.Player


class GameSessionTest {
    @Test
    fun `test generate a move`() = runTest {
         val game = GameSession.create(Random(100))
        val curPlayer: StateFlow<Player> = game.curPlayer
        assertEquals(Player.WHITE, curPlayer.value)
        val gameai = RandomGameAIImpl(game, curPlayer.value)

        val move = gameai.MakeMove()
        println (move)
        if (move != null) {
            game.move(move.first, move.second)
        }
        assertEquals(game.curPlayer.value, Player.BLACK)
    }

    @Test
    fun `test generate a move when not my turn`() = runTest {
        val game = GameSession.create(Random(100))
        val curPlayer: StateFlow<Player> = game.curPlayer
        assertEquals(Player.WHITE, curPlayer.value)
        val gameai = RandomGameAIImpl(game, Player.BLACK)
        val move = gameai.MakeMove()
        assertNull(move)
    }
}
