package org.keizar.game

import org.keizar.game.tilearrangement.StandardTileArrangementFactory
import kotlin.random.Random

data class BoardProperties(
    val width: Int = 8,
    val height: Int = 8,
    val keizarTilePos: BoardPos = BoardPos.fromString("d5"),
    val winningCount: Int = 3,
    val startingPlayer: Player = Player.WHITE,

    val piecesStartingPos: List<Pair<Player, List<BoardPos>>>,
    val tileArrangement: Map<BoardPos, TileType>,
) {

    fun tileBackgroundColor(row: Int, col: Int): Boolean = (row + col) % 2 == 0

    companion object {
        fun getStandardProperties(random: Random): BoardProperties {
            val keizarTilePos: BoardPos = BoardPos.fromString("d5")

            val piecesStartingPos: List<Pair<Player, List<BoardPos>>> = listOf(
                Player.WHITE to BoardPos.range("a1" to "h2"),
                Player.BLACK to BoardPos.range("a7" to "h8"),
            )

            val standardTileArrangement = StandardTileArrangementFactory {
                random(random)
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

            return BoardProperties(
                width = 8,
                height = 8,
                keizarTilePos = keizarTilePos,
                winningCount = 3,
                startingPlayer = Player.WHITE,
                piecesStartingPos = piecesStartingPos,
                tileArrangement = standardTileArrangement
            )
        }
    }
}