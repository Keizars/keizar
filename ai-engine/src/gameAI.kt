package org.keizar.aiengine


import kotlinx.coroutines.flow.last
import org.keizar.game.BoardPos
import org.keizar.game.GameSession
import org.keizar.game.Player

interface GameAI {
    val game: GameSession
    val myplayer: Player

    suspend fun FindBestMove(): Pair<BoardPos, BoardPos>?
    suspend fun MakeMove(): Pair<BoardPos, BoardPos>?
}

class RandomGameAIImpl(
    override val game: GameSession,
    override val myplayer: Player
) : GameAI {

    override suspend fun FindBestMove(): Pair<BoardPos, BoardPos>? {
        val player = game.curPlayer.value
        return if (myplayer == player) {
            val myPieces = game.getAllPiecesPos(myplayer).last()
            var randomPos = myPieces.random()
            var validTargets = game.getAvailableTargets(randomPos).last()
            while (validTargets.isEmpty()) {
                randomPos = myPieces.random()
                validTargets = game.getAvailableTargets(randomPos).last()
            }
            val randomTarget = validTargets.random()
            Pair(randomPos, randomTarget)
        } else null

    }

    override suspend fun MakeMove(): Pair<BoardPos, BoardPos>? {
        val best_move = FindBestMove()
        return best_move
    }

}

