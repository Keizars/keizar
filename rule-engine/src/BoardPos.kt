package org.keizar.game

data class BoardPos(val row: Int, val col: Int) {
    val index get() = row * BoardProperties.BOARD_SIZE + col
    override fun toString(): String {
        // e.g. BoardPos(0, 2) becomes "c1"
        return "${('a'.code + col).toChar()}${row + 1}"
    }

    companion object {
        fun fromString(str: String): BoardPos {
            return BoardPos(str[1].digitToInt() - 1, str[0].code - 'a'.code)
        }
    }
}