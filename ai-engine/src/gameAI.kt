package org.keizar.aiengine

import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.Move
import org.keizar.game.Player
import org.keizar.game.boardPoses

interface GameAI {
    val game: GameSession
    val myplayer: Player

    fun FindBestMove():Move
    fun MakeMove():Move
}

class RandomGameAIImpl(
    override val game: GameSession,
    override val myplayer: Player
):GameAI {

    override fun FindBestMove(): Move {
        TODO("Not yet implemented")
    }

    override fun MakeMove(): Move {
        val best_move = FindBestMove()
        return best_move
    }

}

