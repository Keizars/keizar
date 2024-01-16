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
        val ruleEngineCore = RuleEngineCoreImpl()
        val ruleEngine = RuleEngineImpl(prop, ruleEngineCore)
        val pos1 = BoardPos.fromString("a1")
        val pos2 = BoardPos.fromString("a3")
        ruleEngine.move(pos1, pos2)
        assertEquals(ruleEngine.pieceAt(pos2), Player.WHITE)
        assertEquals(ruleEngine.pieceAt(pos1), null)


    }

//    @Test
//    fun `generate board 2`() {
//        val prop = BoardProperties.getStandardProperties()
//        assertEquals(8, prop.width)
//        assertEquals(8, prop.height)
////        assertEquals("d5", prop.winningPos.toString())
////        assertEquals("{d5=KEIZAR, g7=KING, f4=QUEEN, a2=BISHOP, e6=KNIGHT, a7=ROOK}", prop.tileTypes.toString())
//    }
}