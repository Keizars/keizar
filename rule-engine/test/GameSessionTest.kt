package org.keizar.game

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
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
}