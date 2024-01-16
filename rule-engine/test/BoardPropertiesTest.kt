package org.keizar.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BoardPropertiesTest {
    @Test
    fun `test BoardPos range`() {
        val pos1 = BoardPos.fromString("a1")
        val pos2 = BoardPos.fromString("b4")
        val str = BoardPos.range(pos1 to pos2).joinToString { it.toString() }
        assertEquals(str, "a1, b1, a2, b2, a3, b3, a4, b4")
    }

    @Test
    fun `test BoardPos rangeStr`() {
        val str = BoardPos.rangeStr("a1" to "d2").joinToString { it.toString() }
        assertEquals(str, "a1, b1, c1, d1, a2, b2, c2, d2")
    }

//    @Test
//    fun `generate board`() {
//        val prop = BoardProperties.random(Random(1))
//        assertEquals(8, prop.width)
//        assertEquals(8, prop.height)
//        assertEquals("d5", prop.winningPos.toString())
//        assertEquals("{d5=KEIZAR, a1=KING, f4=QUEEN, e1=BISHOP, e3=KNIGHT, g5=ROOK}", prop.tileTypes.toString())
//    }
//
//    @Test
//    fun `generate board 2`() {
//        val prop = BoardProperties.random(Random(100))
//        assertEquals(8, prop.width)
//        assertEquals(8, prop.height)
//        assertEquals("d5", prop.winningPos.toString())
//        assertEquals("{d5=KEIZAR, g7=KING, f4=QUEEN, a2=BISHOP, e6=KNIGHT, a7=ROOK}", prop.tileTypes.toString())
//    }
}