package org.keizar.game


@JvmInline
value class BoardPos private constructor(
    private val value: Long,
) {
    constructor(row: Int, col: Int) : this((row.toLong() shl 32) or col.toLong())

    val row: Int
        get() = (value shr 32).toInt()

    val col: Int
        get() = value.toInt()

    val color: TileColor
        get() = if ((row + col) % 2 == 0) TileColor.BLACK else TileColor.WHITE

    override fun toString(): String {
        // e.g. BoardPos(0, 2) becomes "c1"
        return "${('a'.code + col).toChar()}${row + 1}"
    }

    companion object {
        fun fromString(str: String): BoardPos {
            return BoardPos(str[1].digitToInt() - 1, str[0].code - 'a'.code)
        }

        fun rangeFrom(range: Pair<BoardPos, BoardPos>): List<BoardPos> {
            return (range.first.row..range.second.row).flatMap { row ->
                (range.first.col..range.second.col).map { col ->
                    BoardPos(row, col)
                }
            }
        }

        fun range(range: Pair<String, String>): List<BoardPos> {
            return rangeFrom(fromString(range.first) to fromString(range.second))
        }
    }
}