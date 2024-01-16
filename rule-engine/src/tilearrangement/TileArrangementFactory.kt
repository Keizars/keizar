package org.keizar.game.tilearrangement

import org.keizar.game.BoardPos
import org.keizar.game.TileType

interface TileArrangementFactory {
    fun build(): Map<BoardPos, TileType>
}
