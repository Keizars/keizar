package org.keizar.aiengine

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.keizar.game.Role
import org.keizar.game.TurnSession


class TurnSessionTest {
    @Test
    fun `test generate a move`() = runTest {
        val game = TurnSession.create(100)
        val curRole: StateFlow<Role> = game.curRole
        assertEquals(Role.WHITE, curRole.value)
        val gameai = RandomGameAIImpl(game, curRole.value)

        val move = gameai.MakeMove()
        println (move)
        if (move != null) {
            game.move(move.first, move.second)
        }
        assertEquals(game.curRole.value, Role.BLACK)
    }

    @Test
    fun `test generate a move when not my turn`() = runTest {
        val game = TurnSession.create(100)
        val curRole: StateFlow<Role> = game.curRole
        assertEquals(Role.WHITE, curRole.value)
        val gameai = RandomGameAIImpl(game, Role.BLACK)
        val move = gameai.MakeMove()
        assertNull(move)
    }
}
