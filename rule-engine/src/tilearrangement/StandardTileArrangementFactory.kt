package org.keizar.game.tilearrangement

import org.keizar.game.BoardPos
import org.keizar.game.TileType
import kotlin.random.Random

class StandardTileArrangementFactory(
    constraints: StandardTileArrangementFactory.() -> Unit
) : TileArrangementFactory {
    private lateinit var random: Random

    private val result: MutableMap<BoardPos, TileType> = mutableMapOf()

    fun randomSeed(seed: Int?) {
        random = if (seed == null) Random else Random(seed)
    }

    fun fixAs(tileType: TileType, positions: () -> Collection<BoardPos>) {
        positions().map { pos -> result[pos] = tileType }
    }

    fun randomlyPut(vararg tiles: TileType, positions: () -> Collection<BoardPos>) {
        randomlyPut(tiles.asList(), positions)
    }

    fun randomlyPut(tiles: Collection<TileType>, positions: () -> Collection<BoardPos>) {
        // Note: if it can't find a position to place the tile, the program will loop infinitely!
        tiles.map { tileType ->
            var pos: BoardPos
            do {
                pos = positions().random(random)
            } while (result.containsKey(pos))
            result[pos] = tileType
        }
    }

    fun fillWith(tileType: TileType, positions: () -> Collection<BoardPos>) {
        positions().map { pos ->
            if (!result.containsKey(pos)) {
                result[pos] = tileType
            }
        }
    }

    init {
        this.apply(constraints)
    }

    override fun build(): Map<BoardPos, TileType> {
        return result
    }
}
