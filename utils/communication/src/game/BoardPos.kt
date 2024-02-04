package org.keizar.utils.communication.game

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@JvmInline
@Serializable// (BoardPos.AsArraySerializer::class)
value class BoardPos private constructor(
    private val value: Long,
) {
    constructor(row: Int, col: Int) : this((row.toLong() shl 32) or col.toLong())

    constructor(str: String) : this(str[1].digitToInt() - 1, str[0].code - 'a'.code)

    val row: Int
        get() = (value shr 32).toInt()

    /**
     * Starts from 0
     */
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
            return BoardPos(str)
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

    object AsArraySerializer : KSerializer<BoardPos> {
        private val delegate = IntArraySerializer()
        override val descriptor: SerialDescriptor = delegate.descriptor

        override fun deserialize(decoder: Decoder): BoardPos {
            delegate.deserialize(decoder).run {
                return BoardPos(row = get(0), col = get(1))
            }
        }

        override fun serialize(encoder: Encoder, value: BoardPos) {
          return  delegate.serialize(encoder, intArrayOf(value.row, value.col))
        }
    }
}