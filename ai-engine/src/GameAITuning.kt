package org.keizar.aiengine

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.keizar.game.GameSession
import org.keizar.utils.communication.game.GameResult
import org.keizar.utils.communication.game.Player
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.seconds

class HyperParameterTuning {
    fun better_parameter_tuning() = runTest(timeout = 1000.seconds) {
        var ai1Win = 0
        var ai2Win = 0
        for (i in 1..500) {
            println("Session: $i ")
            val game = GameSession.create(0)
            val context = EmptyCoroutineContext
            val ai1Parameter = AIParameters(
                keizarThreshold = 1,
                possibleMovesThreshold = 5,
                noveltyLevel = 0.9
            )
            val ai2Parameter = AIParameters(
                keizarThreshold = 2,
                possibleMovesThreshold = 5,
                noveltyLevel = 0.9
            )
            val ai1 = AlgorithmAI(game, Player.FirstWhitePlayer, context, true, ai1Parameter)
            val ai2 = AlgorithmAI(game, Player.FirstBlackPlayer, context, true, ai2Parameter)
            ai1.start()
            ai2.start()
            val winner = game.finalWinner.filterNotNull().first()
            if (winner == GameResult.Winner(Player.FirstWhitePlayer)) {
                ai1Win++
            } else if (winner == GameResult.Winner(Player.FirstBlackPlayer)) {
                ai2Win++
            }
            ai1.end()
            ai2.end()
        }
        println("ai1Win: $ai1Win")
        println("ai2Win: $ai2Win")
    }
}

fun main() {
    val hyperParameterTuning = HyperParameterTuning()
    hyperParameterTuning.better_parameter_tuning()
}