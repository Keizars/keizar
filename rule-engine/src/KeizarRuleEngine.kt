package org.keizar.game

import kotlinx.coroutines.flow.Flow
import org.keizar.game.local.Board
import kotlin.random.Random

interface KeizarRuleEngine {
    val board: Board
    val win: Flow<Boolean>

    suspend fun undo(): Boolean
    suspend fun redo(): Boolean

    fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>>
    suspend fun move(from: BoardPos, to: BoardPos): Boolean
}

data class BoardPos(
    val row: Int,
    val column: Int,
)

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
            column % 2 == 0
        } else {
            column % 2 != 0
        }
    }

    companion object {
        fun random(random: Random = Random) {
            val winningPos = BoardPos(4, 3)
            val map = generateSequence {
                mapOf(
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.KING,
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.QUEEN,
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.BISHOP,
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.KNIGHT,
                    BoardPos(random.nextInt(), random.nextInt()) to TileType.ROOK,
                )
            }

            fun Map<BoardPos, TileType>.isValid(): Boolean {
                return keys.all { it != winningPos } && keys.distinct().size == this.size
            }

            map.filter { it.isValid() }

            BoardProperties(
                8, 8,
                winningPos = winningPos,
                tileTypes = map.first(),
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