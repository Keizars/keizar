package org.keizar.aiengine


import kotlinx.coroutines.flow.toList
import org.keizar.game.BoardPos
import org.keizar.game.GameSession
import org.keizar.game.Player
import kotlin.random.Random

interface GameAI {
    val game: GameSession
    val myplayer: Player

    suspend fun FindBestMove(): Pair<BoardPos,BoardPos>?
    suspend fun MakeMove():Pair<BoardPos,BoardPos>?
}

class RandomGameAIImpl(
    override val game: GameSession,
    override val myplayer: Player
):GameAI {

    override suspend fun FindBestMove():Pair<BoardPos,BoardPos>? {
        val player = game.curPlayer.value
        return if (myplayer == player){
            val myPieces = game.getAllPiecesPos(myplayer).toList()[0]
            val randomItem = myPieces[Random.nextInt(myPieces.size)]
            val validTargets = game.getAvailableTargets(randomItem).toList()[0]
            val randomTarget = validTargets.random()
            Pair(randomItem, randomTarget)
        } else null

    }

    override suspend fun MakeMove(): Pair<BoardPos,BoardPos>? {
        val best_move = FindBestMove()
        return best_move
    }

}

