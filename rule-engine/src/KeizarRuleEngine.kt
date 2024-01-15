package org.keizar.game

import kotlinx.coroutines.flow.Flow
import org.keizar.game.local.Board
import org.keizar.game.local.Piece
import kotlin.random.Random

interface KeizarRuleEngine {
    val board: Board
    val win: Flow<Boolean>

    suspend fun undo(): Boolean
    suspend fun redo(): Boolean

    fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>>
    suspend fun move(from: BoardPos, to: BoardPos): Boolean
}

interface Board {
    val properties: BoardProperties

    val tiles: Flow<List<BoardPos>>
}

class BoardProperties(
    val width: Int,
    val height: Int,
    val winningPos: BoardPos,
    val tileTypes: Map<BoardPos, TileType>
) {
    fun tileBackgroundColor(row: Int, column: Int): Boolean {
        return if (row % 2 == 0) {
            column % 2 != 0
        } else {
            column % 2 == 0
        }
    }

    companion object {
        const val BOARD_SIZE = 8
        const val WINNING_COUNT = 3

        val KEIZAR_POS = BoardPos.fromString("d5")
        val STARTING_PLAYER = Piece.Color.WHITE
        val PIECES_STARTING_POS = listOf(
            Piece.Color.WHITE to listOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            ).map { BoardPos.fromString(it) },
            Piece.Color.BLACK to listOf(
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
            ).map { BoardPos.fromString(it) }
        )
        val FIXED_PLAIN_TILES_POS = listOf(
            "a1", "b1", "g1", "h1", "c2", "d2", "e2", "f2",
            "a8", "b8", "g8", "h8", "c7", "d7", "e7", "f7",
        ).map { BoardPos.fromString(it) }

        fun random(random: Random = Random): BoardProperties {
            val map = generateSequence {
                mapOf(
                    BoardPos(4, 3) to TileType.KEIZAR,
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.KING,
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.QUEEN,
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.BISHOP,
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.KNIGHT,
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.ROOK,
                )
            }

            fun Map<BoardPos, TileType>.isValid(): Boolean {
                return keys.distinct().size == this.size
            }

            val gen = map.filter { it.isValid() }.first()
            return BoardProperties(
                8, 8,
                winningPos = gen.entries.first { it.value == TileType.KEIZAR }.key,
                tileTypes = gen,
            )
        }
    }
}

enum class TileType {
    KING,
    QUEEN,
    BISHOP,
    KNIGHT,
    ROOK,
    KEIZAR,
    NORMAL,
}