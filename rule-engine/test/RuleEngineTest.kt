package org.keizar.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.internal.RuleEngineImpl
import kotlin.random.Random

class RuleEngineTest {
    @Test
    fun `generate board`() {
        val prop = BoardProperties.getStandardProperties(1)
        assertEquals(8, prop.width)
        assertEquals(8, prop.height)
        assertEquals("d5", prop.keizarTilePos.toString())
        val ruleEngineCore = RuleEngineCoreImpl(prop)
        val ruleEngine = RuleEngineImpl(prop, ruleEngineCore)
        val pos1 = BoardPos.fromString("a2")
        val pos2 = BoardPos.fromString("a3")
        ruleEngine.move(pos1, pos2)
        assertEquals(ruleEngine.pieceAt(pos2), Player.WHITE)
        assertEquals(ruleEngine.pieceAt(pos1), null)

    }

    @Test
    fun `check standard tile arrangement`() {
        val prop = BoardProperties.getStandardProperties(1)
        assertEquals("d5", prop.keizarTilePos.toString())
        val arrangement = prop.tileArrangement
        val checkList = MutableList(7){ MutableList(2){0} }
        arrangement.forEach{
            when(it.value){
                TileType.KING -> if(it.key.color == TileColor.WHITE) checkList[0][0]++ else checkList[0][1] ++
                TileType.QUEEN -> if(it.key.color == TileColor.WHITE) checkList[1][0]++ else checkList[1][1] ++
                TileType.BISHOP -> if(it.key.color == TileColor.WHITE) checkList[2][0]++ else checkList[2][1] ++
                TileType.KNIGHT -> if(it.key.color == TileColor.WHITE) checkList[3][0]++ else checkList[3][1] ++
                TileType.ROOK -> if(it.key.color == TileColor.WHITE) checkList[4][0]++ else checkList[4][1] ++
                TileType.KEIZAR -> if(it.key.color == TileColor.WHITE) checkList[5][0]++ else checkList[5][1] ++
                TileType.PLAIN -> if(it.key.color == TileColor.WHITE) checkList[6][0]++ else checkList[6][1] ++
            }
        }
        assertEquals(checkList[0][0], 1)
        assertEquals(checkList[0][1], 1)
        assertEquals(checkList[1][0], 1)
        assertEquals(checkList[1][1], 1)
        assertEquals(checkList[2][0], 2)
        assertEquals(checkList[2][1], 2)
        assertEquals(checkList[3][0], 2)
        assertEquals(checkList[3][1], 2)
        assertEquals(checkList[4][0], 2)
        assertEquals(checkList[4][1], 2)
        assertEquals(checkList[5][0], 1)
        assertEquals(checkList[5][1], 0)
        assertEquals(checkList[6][0], 23)
        assertEquals(checkList[6][1], 24)
    }

    @Test
    fun `test move`() {
        val prop = BoardProperties.getStandardProperties(100)
        val ruleEngineCore = RuleEngineCoreImpl(prop)
        val ruleEngine = RuleEngineImpl(prop, ruleEngineCore)

        assertTrue(ruleEngine.move(BoardPos("c2"), BoardPos("c4")))
        assertEquals(ruleEngine.pieceAt(BoardPos("c2")), null)
        assertEquals(ruleEngine.pieceAt(BoardPos("c4")), Player.WHITE)

        assertTrue(ruleEngine.move(BoardPos("e7"), BoardPos("e5")))
        assertEquals(ruleEngine.pieceAt(BoardPos("e7")), null)
        assertEquals(ruleEngine.pieceAt(BoardPos("e5")), Player.BLACK)

        assertTrue(ruleEngine.move(BoardPos("h2"), BoardPos("h3")))
        assertEquals(ruleEngine.pieceAt(BoardPos("h2")), null)
        assertEquals(ruleEngine.pieceAt(BoardPos("h3")), Player.WHITE)
    }

    @Test
    fun `test showPossibleMoves`() {
        val prop = BoardProperties.getStandardProperties(100)
        val ruleEngineCore = RuleEngineCoreImpl(prop)
        val ruleEngine = RuleEngineImpl(prop, ruleEngineCore)

        assertTrue(ruleEngine.move(BoardPos("d2"), BoardPos("d3")))
        assertEquals(
            listOf("d2", "d4", "c3", "c4", "e3", "e4").map { BoardPos.fromString(it) }.toSet(),
            ruleEngine.showPossibleMoves(BoardPos("d3")).toSet(),
        )
    }
}
