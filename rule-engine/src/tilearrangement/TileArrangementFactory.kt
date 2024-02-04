package org.keizar.game.tilearrangement

import org.keizar.game.TileType
import org.keizar.utils.communication.game.BoardPos

interface TileArrangementFactory {
    fun build(): Map<BoardPos, TileType>
}
