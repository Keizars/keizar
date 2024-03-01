package org.keizar.utils.communication.game
import kotlinx.serialization.Serializable

@Serializable
data class PlayerStatistics(
    val player: Player,

    val round1Captured: Int,
    val round2Captured: Int,

    val round1OpponentCaptured: Int,
    val round2OpponentCaptured: Int,

    val round1Moves: Int,
    val round2Moves: Int,

    val round1Time: Int,
    val round2Time: Int,

    val round1AverageTime: Long,
    val round2AverageTime: Long,
)
