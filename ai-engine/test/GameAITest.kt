package org.keizar.aiengine

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.keizar.game.GameSession
import org.keizar.game.Player
import org.keizar.game.Role
import org.keizar.game.RoundSession
import kotlin.coroutines.EmptyCoroutineContext


class TurnSessionTest {
    @Test
    fun `test generate a AI`() = runTest {
        val game = GameSession.create(100)
        val context = EmptyCoroutineContext
        val ai1 = RandomGameAIImpl(game, Player.Player1, context)
        val ai2 = RandomGameAIImpl(game, Player.Player2, context)
        ai1.start()
        ai2.start()

    }
}
