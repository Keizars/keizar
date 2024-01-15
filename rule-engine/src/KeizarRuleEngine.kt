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
    val width: Int = 8,
    val height: Int = 8,
    val winningPos: BoardPos = BoardPos.fromString("d5"),
    val tileTypes: Map<BoardPos, TileType>,
    val winningCount: Int = 3,
    val startingPlayer: Piece.Color = Piece.Color.WHITE,
    val piecesStartingPos: List<Pair<Piece.Color, List<BoardPos>>> = listOf(
        Piece.Color.WHITE to listOf(
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
        ).map { BoardPos.fromString(it) },
        Piece.Color.BLACK to listOf(
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
        ).map { BoardPos.fromString(it) }
    ),
    val fixedPlainTilePos: List<BoardPos> = listOf(
        "a1", "b1", "g1", "h1", "c2", "d2", "e2", "f2",
        "a8", "b8", "g8", "h8", "c7", "d7", "e7", "f7",
    ).map { BoardPos.fromString(it) },
) {
    fun tileBackgroundColor(row: Int, column: Int): Boolean {
        return if (row % 2 == 0) {
            column % 2 == 0
        } else {
            column % 2 != 0
        }
    }

    companion object {        
data class BoardProperties(
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
            val width = 8
            val height = 8
            val keizarPos = BoardPos(4, 3)

            val usedPositions = mutableSetOf(keizarPos)
            fun nextPos(): BoardPos {
                while (true) {
                    val pos = BoardPos(random.nextInt(0, height - 1), random.nextInt(0, width - 1))
                    if (usedPositions.add(pos)) {
                        return pos
                    }
                }
            }

            val map = generateSequence {
                mapOf(
                    keizarPos to TileType.KEIZAR,
                    nextPos() to TileType.KING,
                    nextPos() to TileType.QUEEN,
                    nextPos() to TileType.BISHOP,
                    nextPos() to TileType.KNIGHT,
                    nextPos() to TileType.ROOK,
                )
            }

            val gen = map.first()
            return BoardProperties(
                width, height,
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