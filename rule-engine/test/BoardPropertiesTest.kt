package org.keizar.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.local.RuleEngineImpl
import kotlin.random.Random

class BoardPropertiesTest {
//    @Test
//    fun `test BoardPos range`() {
//        val pos1 = BoardPos.fromString("a1")
//        val pos2 = BoardPos.fromString("b4")
//        val str = BoardPos.range(Pair("a1", "b4")).joinToString { it.toString() }
//        assertEquals(str, "a1, b1, a2, b2, a3, b3, a4, b4")
//    }
//
//    @Test
//    fun `test BoardPos rangeStr`() {
//        val str = BoardPos.rangeFrom("a1" to "d2").joinToString { it.toString() }
//        assertEquals(str, "a1, b1, c1, d1, a2, b2, c2, d2")
//    }

    @Test
    fun `generate board`() {
        val prop = BoardProperties.getStandardProperties(Random(1))
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
        val prop = BoardProperties.getStandardProperties(Random(1))
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
}