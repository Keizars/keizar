package org.keizar.game

import kotlinx.serialization.Serializable
import org.keizar.game.tilearrangement.PlainTileArrangementFactory
import org.keizar.game.tilearrangement.StandardTileArrangementFactory
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.TileColor
import kotlin.math.absoluteValue
import kotlin.random.Random

/***
 * Defines the basic game rules
 */
@Serializable
data class BoardProperties(
    // board width
    override val width: Int = BoardPropertiesPrototypes.Plain.width,

    // board height
    override val height: Int = BoardPropertiesPrototypes.Plain.height,

    // position of the keizar tile
    override val keizarTilePos: BoardPos = BoardPropertiesPrototypes.Plain.keizarTilePos,

    // number of turns needed to stay on th keizar tile in order to win the game
    override val winningCount: Int = BoardPropertiesPrototypes.Plain.winningCount,

    // role (black/white) that plays first
    override val startingRole: Role = BoardPropertiesPrototypes.Plain.startingRole,

    // number of rounds in one complete game
    // meaningful values are 1 or 2
    override val rounds: Int = BoardPropertiesPrototypes.Plain.rounds,

    // list of initial positions of the pieces for each role (black/white)
    override val piecesStartingPos: Map<Role, List<BoardPos>>,

    // arrangement of tiles, could be generated by a TileArrangement factory
    override val tileArrangement: Map<BoardPos, TileType>,

    // random seed used by TileArrangement factory
    override val seed: Int? = null,
) : AbstractBoardProperties {

    fun tileBackgroundColor(row: Int, col: Int): Boolean = (row + col) % 2 == 0

    companion object {
        fun generateRandomSeed(): Int =
            Random.nextInt().absoluteValue // Use absolute value so easier to share

        fun getPlainBoard(): BoardProperties {
            return BoardPropertiesBuilder(prototype = BoardPropertiesPrototypes.Plain).build()
        }

        fun getStandardProperties(randomSeed: Int? = null): BoardProperties {
            val seed = randomSeed ?: generateRandomSeed()
            return BoardPropertiesBuilder(prototype = BoardPropertiesPrototypes.Standard(seed)).build()
        }
    }
}


object BoardPropertiesPrototypes {
    data object Plain : AbstractBoardProperties {
        override val width: Int = 8
        override val height: Int = 8
        override val keizarTilePos: BoardPos = BoardPos.fromString("d5")
        override val winningCount: Int = 3
        override val startingRole: Role = Role.WHITE
        override val rounds: Int = 2
        override val piecesStartingPos: Map<Role, List<BoardPos>> = mapOf(
            Role.WHITE to BoardPos.range("a1" to "h2"),
            Role.BLACK to BoardPos.range("a7" to "h8"),
        )
        override val tileArrangement: Map<BoardPos, TileType> = PlainTileArrangementFactory(
            boardWidth = width,
            boardHeight = height,
            winningPos = keizarTilePos
        ).build()
        override val seed: Int? = null
    }

    data class Standard(
        override val seed: Int
    ) : AbstractBoardProperties by Plain {
        override val tileArrangement: Map<BoardPos, TileType> = StandardTileArrangementFactory {
            random(Random(seed))
            fixAs(TileType.KEIZAR) {
                listOf(keizarTilePos)
            }
            fixAs(TileType.PLAIN) {
                listOf(
                    "a1", "b1", "g1", "h1", "c2", "d2", "e2", "f2",
                    "a8", "b8", "g8", "h8", "c7", "d7", "e7", "f7",
                ).map { BoardPos.fromString(it) }
            }
            randomlyPut(TileType.QUEEN, TileType.BISHOP, TileType.KNIGHT, TileType.ROOK) {
                BoardPos.range("a1" to "h4").filter { it.color == TileColor.BLACK }
            }
            randomlyPut(TileType.KING, TileType.BISHOP, TileType.KNIGHT, TileType.ROOK) {
                BoardPos.range("a1" to "h4").filter { it.color == TileColor.WHITE }
            }
            randomlyPut(TileType.KING, TileType.BISHOP, TileType.KNIGHT, TileType.ROOK) {
                BoardPos.range("a5" to "h8").filter { it.color == TileColor.BLACK }
            }
            randomlyPut(TileType.QUEEN, TileType.BISHOP, TileType.KNIGHT, TileType.ROOK) {
                BoardPos.range("a5" to "h8").filter { it.color == TileColor.WHITE }
            }
            fillWith(TileType.PLAIN) {
                BoardPos.range("a1" to "h8")
            }
        }.build()
    }
}

interface AbstractBoardProperties {
    val width: Int
    val height: Int
    val keizarTilePos: BoardPos
    val winningCount: Int
    val startingRole: Role
    val rounds: Int
    val piecesStartingPos: Map<Role, List<BoardPos>>
    val tileArrangement: Map<BoardPos, TileType>
    val seed: Int?
}
