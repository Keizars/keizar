package org.keizar.utils.communication.game
import kotlinx.serialization.Serializable

//@Serializable
//data class GameStats(
//    val player: Player,
//
//    val round1Captured: Int,
//    val round2Captured: Int,
//
//    val round1OpponentCaptured: Int,
//    val round2OpponentCaptured: Int,
//
//    val round1Moves: Int,
//    val round2Moves: Int,
//
//    val round1Time: Int,
//    val round2Time: Int,
//
//    val round1AverageTime: Long,
//    val round2AverageTime: Long,
//)

@Serializable
data class NeutralStats(
    val whiteCaptured: Int,
    val blackCaptured: Int,
    val whiteAverageTime: Double,
    val whiteMoves: Int,
    val blackMoves: Int,
    val blackAverageTime: Double,
    val blackTime: Int,
    val whiteTime: Int,
)


@Serializable
data class RoundStats(
    val neutralStats: NeutralStats,
    val player: Player,
    val winner: Player?,
)
