package org.keizar.game

interface InitialTilesFactory {
    fun build(): Map<BoardPos, TileType>
}

class PlainInitialTilesFactory(
    private val boardWidth: Int,
    private val boardHeight: Int,
    private val winningPos: BoardPos = BoardPos.fromString("d5"),
) : InitialTilesFactory {
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