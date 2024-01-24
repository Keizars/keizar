package org.keizar.aiengine

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.game.TurnSession

//
//class TurnSessionTest {
//    @Test
//    fun `test generate a move`() = runTest {
//        val game = GameSession.create(100)
//        val turn = game.currentTurn.value
//        val curRole: StateFlow<Role> = turn.curRole
//        assertEquals(Role.WHITE, curRole.value)
//        val gameai = RandomGameAIImpl(turn, curRole.value)
//
//        val move = gameai.MakeMove()
//        println (move)
//        if (move != null) {
//            turn.move(move.first, move.second)
//        }
//        assertEquals(turn.curRole.value, Role.BLACK)
//    }
//
//    @Test
//    fun `test generate a move when not my turn`() = runTest {
//        val game = GameSession.create(100)
//        val turn = game.currentTurn.value
//        val curRole: StateFlow<Role> = turn.curRole
//        assertEquals(Role.WHITE, curRole.value)
//        val gameai = RandomGameAIImpl(turn, Role.BLACK)
//        val move = gameai.MakeMove()
//        assertNull(move)
//    }
//}
