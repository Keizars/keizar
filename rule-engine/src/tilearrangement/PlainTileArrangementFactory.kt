package org.keizar.game.tilearrangement

import org.keizar.game.BoardPos
import org.keizar.game.TileType

class PlainTileArrangementFactory(
    private val boardWidth: Int,
    private val boardHeight: Int,
    private val winningPos: BoardPos = BoardPos.fromString("d5"),
) : TileArrangementFactory {
    override fun build(): Map<BoardPos, TileType> {
        val tiles = mutableMapOf<BoardPos, TileType>()
        for (row in 0..<boardWidth) {
            for (col in 0..<boardHeight) {
                tiles[BoardPos(row, col)] = TileType.PLAIN
            }
        }
        tiles[winningPos] = TileType.KEIZAR
        return tiles
    }
}