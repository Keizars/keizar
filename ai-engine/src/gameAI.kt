package org.keizar.aiengine


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.keizar.game.BoardPos
import org.keizar.game.GameSession
import org.keizar.game.Player
import org.keizar.game.Role
import org.keizar.game.RoundSession
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.random.nextLong

interface GameAI {
    val game: GameSession
    val myPlayer: Player

    fun start()

    suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos>?

    suspend fun end()

}

class RandomGameAIImpl(
    override val game: GameSession,
    override val myPlayer: Player,
    private val parentCoroutineContext: CoroutineContext,
    private val test: Boolean = false
) : GameAI {

    private val myCoroutione: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    override fun start() {
        myCoroutione.launch {
            game.currentRole(myPlayer).collect { myRole ->
                game.currentRound.collect {
                    it.curRole.collect { role ->
                        if (myRole == role) {
                            val bestPos = findBestMove(it, role)
                            if (!test) {
                                delay(Random.nextLong(1500L..3000L))
                            }
                            it.move(bestPos.first, bestPos.second)
                        }
                    }
                }
            }
        }

        myCoroutione.launch {
            game.currentRole(myPlayer).collect { myRole ->
                game.currentRound.collect{
                    it.winner.collect{
                        if (it != null) {
                            game.confirmNextRound(myPlayer)
                        }
                    }
                }
            }
        }
    }

    override suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos> {
        val allPieces = round.getAllPiecesPos(role).first()
        var randomPiece = allPieces.random()
        var validTargets = round.getAvailableTargets(randomPiece).first()
        do {
            randomPiece = allPieces.random()
            validTargets = round.getAvailableTargets(randomPiece).first()
        } while (validTargets.isEmpty() && allPieces.isNotEmpty())

        val randomTarget = validTargets.random()
        return Pair(randomPiece, randomTarget)
    }

    override suspend fun end() {
        myCoroutione.cancel()
    }


}

