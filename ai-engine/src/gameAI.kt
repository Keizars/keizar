package org.keizar.aiengine


import kotlinx.coroutines.flow.last
import org.keizar.game.BoardPos
import org.keizar.game.Role
import org.keizar.game.RoundSession
import org.keizar.game.GameSession
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

interface GameAI {
    val game: GameSession
    val myPlayer: Role

    suspend fun start()

    suspend fun FindBestMove(round: RoundSession): Pair<BoardPos, BoardPos>?

}

class RandomGameAIImpl(
    override val game: GameSession,
    override val myPlayer: Role,
    val parentCoroutineContext: CoroutineContext,
) : GameAI {

    override suspend fun start() {
        val myCoroutione: CoroutineScope = CoroutineScope(parentCoroutineContext)
        myCoroutione.launch {
            val round: RoundSession = game.currentRound.last()
            val player = round.curRole.value
            if (myPlayer == player) {
                val best_pos = FindBestMove(round)
                if (best_pos != null) {
                    round.move(best_pos.first, best_pos.second)
                }
            }

        }
    }

    override suspend fun FindBestMove(round: RoundSession): Pair<BoardPos, BoardPos>? {
        val player = round.curRole.value
        val myPieces = round.getAllPiecesPos(myPlayer).last()
        var randomPos = myPieces.random()
        var validTargets = round.getAvailableTargets(randomPos).last()
        while (validTargets.isEmpty()) {
            randomPos = myPieces.random()
            validTargets = round.getAvailableTargets(randomPos).last()
        }
        val randomTarget = validTargets.random()
        return Pair(randomPos, randomTarget)
    }


}

