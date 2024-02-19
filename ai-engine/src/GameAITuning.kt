package org.keizar.aiengine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.keizar.game.GameSession
import org.keizar.utils.communication.game.GameResult
import org.keizar.utils.communication.game.Player
import kotlin.coroutines.EmptyCoroutineContext

suspend fun parameterComparison(keizarThreshold1 : Int,
                        possibleMovesThreshold1: Int,
                        noveltyLevel1: Double,
                        keizarThreshold2 : Int,
                        possibleMovesThreshold2: Int,
                        noveltyLevel2: Double): Triple<Int, Int, Double> {
    var ai1Win = 0
    var ai2Win = 0
    for (i in 1..200) {
        println("Session: $i ")
        //generate random seed in range 1 to 1000
        val randomSeed = (1..1000).random()
        val game = GameSession.create(randomSeed)
        val context = EmptyCoroutineContext
        val ai1Parameter = AIParameters(
            keizarThreshold = keizarThreshold1,
            possibleMovesThreshold = possibleMovesThreshold1,
            noveltyLevel = noveltyLevel1
        )
        val ai2Parameter = AIParameters(
            keizarThreshold = keizarThreshold2,
            possibleMovesThreshold = possibleMovesThreshold2,
            noveltyLevel = noveltyLevel2
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

    return if (ai1Win > ai2Win) {
        Triple(keizarThreshold1, possibleMovesThreshold1, noveltyLevel1)
    } else {
        Triple(keizarThreshold2, possibleMovesThreshold2, noveltyLevel2)
    }
}


suspend fun randomSearch(paramSpaceX: IntRange, paramSpaceY: IntRange, paramSpaceZ: IntRange): Triple<Int, Int, Double> {
    var bestParams = Triple(1, 5, 0.9)
    var bestScore = Int.MAX_VALUE

    // randomly select a combination of parameters from x y and z
    repeat(100) {
        val x = paramSpaceX.random()
        val y = paramSpaceY.random()
        val z = paramSpaceZ.random() / 100.0
        print("opponents: ($x, $y, $z) vs $bestParams")
        bestParams = parameterComparison(
            x,
            y,
            z,
            bestParams.first,
            bestParams.second,
            bestParams.third
        )
    }

    return bestParams
}


fun main() {
    val keizarThreshold = -1 .. 1
    val possibleMovesThreshold = 1 .. 10
    val noveltyLevel = 80 .. 100

    runBlocking { // Creates a new coroutine scope
        launch { // Launches a new coroutine in the scope of runBlocking
            val (bestKeizarThreshold, bestPossibleMovesThreshold, bestNoveltyLevel) =
                randomSearch(keizarThreshold, possibleMovesThreshold, noveltyLevel)
            println("Best parameters: $bestKeizarThreshold, $bestPossibleMovesThreshold, $bestNoveltyLevel")
        }
    }

}