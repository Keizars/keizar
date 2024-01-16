package org.keizar.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BoardPosTest {
    @Test
    fun `test fromString`() {
        val pos = BoardPos.fromString("c1")
        assertEquals(0, pos.row)
        assertEquals(2, pos.col)
    }

    @Test
    fun `test range`() {
        val pos1 = BoardPos.fromString("a1")
        val pos2 = BoardPos.fromString("b4")
        val str = BoardPos.rangeFrom(pos1 to pos2).joinToString { it.toString() }
        assertEquals("a1, b1, a2, b2, a3, b3, a4, b4", str)
    }

    @Test
    fun `test rangeStr`() {
        val str = BoardPos.range("a1" to "d2").joinToString { it.toString() }
        assertEquals("a1, b1, c1, d1, a2, b2, c2, d2", str)
    }
}