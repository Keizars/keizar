package org.keizar.game

import org.keizar.game.tilearrangement.StandardTileArrangementFactory
import org.keizar.game.tilearrangement.TileArrangementFactory

data class BoardProperties(
    val width: Int = 8,
    val height: Int = 8,
    val keizarTilePos: BoardPos = BoardPos.fromString("d5"),
    val winningCount: Int = 3,
    val startingPlayer: Player = Player.WHITE,

    val piecesStartingPos: List<Pair<Player, List<BoardPos>>>,
    private val tileArrangementFactory: TileArrangementFactory,
) {
    val tileArrangement: Map<BoardPos, TileType> get() = tileArrangementFactory.build()

    fun tileBackgroundColor(row: Int, col: Int): Boolean = (row + col) % 2 == 0

    companion object {
        fun getStandardProperties(randomSeed: Int? = null): BoardProperties {
            val keizarTilePos: BoardPos = BoardPos.fromString("d5")

            val piecesStartingPos: List<Pair<Player, List<BoardPos>>> = listOf(
                Player.WHITE to BoardPos.rangeStr("a1" to "h2"),
                Player.BLACK to BoardPos.rangeStr("a7" to "h8"),
            )

            val standardTileArrangementFactory = StandardTileArrangementFactory {
                if (randomSeed != null) randomSeed(randomSeed)
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
                    BoardPos.rangeStr("a1" to "h4").filter { it.color == TileColor.BLACK }
                }
                randomlyPut(TileType.KING, TileType.BISHOP, TileType.KNIGHT, TileType.ROOK) {
                    BoardPos.rangeStr("a1" to "h4").filter { it.color == TileColor.WHITE }
                }
                randomlyPut(TileType.KING, TileType.BISHOP, TileType.KNIGHT, TileType.ROOK) {
                    BoardPos.rangeStr("a5" to "h8").filter { it.color == TileColor.BLACK }
                }
                randomlyPut(TileType.QUEEN, TileType.BISHOP, TileType.KNIGHT, TileType.ROOK) {
                    BoardPos.rangeStr("a5" to "h8").filter { it.color == TileColor.WHITE }
                }
                fillWith(TileType.PLAIN) {
                    BoardPos.rangeStr("a1" to "h8")
                }
            }

            return BoardProperties(
                width = 8,
                height = 8,
                keizarTilePos = keizarTilePos,
                winningCount = 3,
                startingPlayer = Player.WHITE,
                piecesStartingPos = piecesStartingPos,
                tileArrangementFactory = standardTileArrangementFactory
            )
        }
    }
}