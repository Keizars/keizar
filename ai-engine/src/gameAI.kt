package org.keizar.aiengine


import kotlinx.coroutines.flow.last
import org.keizar.game.BoardPos
import org.keizar.game.Role
import org.keizar.game.RoundSession
import org.keizar.game.GameSession
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlin.coroutines.CoroutineContext

interface GameAI {
    val game: GameSession
    val myPlayer: Role

    suspend fun start()

    suspend fun FindBestMove(round: RoundSession): Pair<BoardPos, BoardPos>?

    suspend fun end()

}

class RandomGameAIImpl(
    override val game: GameSession,
    override val myPlayer: Role,
    private val parentCoroutineContext: CoroutineContext,
) : GameAI {

    override suspend fun start() {
        val myCoroutione: CoroutineScope = CoroutineScope(parentCoroutineContext)
        myCoroutione.launch {
            game.currentRound.collect{
                val player = it.curRole.value
                if (myPlayer == player) {
                    val bestPos = FindBestMove(it)
                    it.move(bestPos.first, bestPos.second)
                }
            }
        }
    }

    override suspend fun FindBestMove(round: RoundSession): Pair<BoardPos, BoardPos> {
        val allPieces = round.getAllPiecesPos(myPlayer).first()
        var randomPiece = allPieces.random()
        val randomTarget = round.getAvailableTargets(randomPiece)
                .filter { it.isNotEmpty() }
                .first()
                .random()
        return Pair(randomPiece, randomTarget)
    }

    override suspend fun end() {
        TODO("Not yet implemented")
    }


}

