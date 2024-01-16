package org.keizar.game

import kotlin.random.Random

data class BoardProperties(
    val width: Int = 8,
    val height: Int = 8,
    val winningPos: BoardPos = BoardPos.fromString("d5"),
    val winningCount: Int = 3,
    val startingPlayer: Player = Player.WHITE,

    val random: Random = Random(0xBEEF),
    val piecesStartingPos: List<Pair<Player, List<BoardPos>>> = listOf(
        Player.WHITE to listOf(
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
        ).map { BoardPos.fromString(it) },
        Player.BLACK to listOf(
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
        ).map { BoardPos.fromString(it) }
    ),
    val fixedPlainTilePos: List<BoardPos> = listOf(
        "a1", "b1", "g1", "h1", "c2", "d2", "e2", "f2",
        "a8", "b8", "g8", "h8", "c7", "d7", "e7", "f7",
    ).map { BoardPos.fromString(it) },
    private val initialTilesFactory: InitialTilesFactory = PlainInitialTilesFactory(width, height),
) {
    val tileTypes: Map<BoardPos, TileType> get() = initialTilesFactory.build()

    fun tileBackgroundColor(row: Int, column: Int): Boolean {
        return if (row % 2 == 0) {
            column % 2 == 0
        } else {
            column % 2 != 0
        }
    }

    companion object {
        fun random(random: Random = Random): BoardProperties {
            return BoardProperties(random = random)
        }
    }
}