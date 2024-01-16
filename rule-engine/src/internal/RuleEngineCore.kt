package org.keizar.game.internal

import org.keizar.game.BoardPos
import org.keizar.game.local.Piece
import org.keizar.game.local.Tile

interface RuleEngineCore {
    fun showValidMoves(tiles: List<Tile>, piece: Piece): List<BoardPos>
}

class RuleEngineCoreImpl : RuleEngineCore {
    override fun showValidMoves(tiles: List<Tile>, piece: Piece): List<BoardPos> {
        TODO("Not yet implemented")
    }
}
