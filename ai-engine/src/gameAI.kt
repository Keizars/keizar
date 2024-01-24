package org.keizar.aiengine


import kotlinx.coroutines.flow.last
import org.keizar.game.BoardPos
import org.keizar.game.Role
import org.keizar.game.TurnSession

interface GameAI {
    val game: TurnSession
    val myplayer: Role

    suspend fun FindBestMove(): Pair<BoardPos, BoardPos>?
    suspend fun MakeMove(): Pair<BoardPos, BoardPos>?
}

class RandomGameAIImpl(
    override val game: TurnSession,
    override val myplayer: Role
) : GameAI {

    override suspend fun FindBestMove(): Pair<BoardPos, BoardPos>? {
        val player = game.curRole.value
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

