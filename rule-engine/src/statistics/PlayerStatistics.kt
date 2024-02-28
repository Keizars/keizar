package org.keizar.game.statistics

import kotlinx.serialization.Serializable
import org.keizar.game.GameSession
import org.keizar.utils.communication.game.Player

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


/**
 * TODO: add comments
 */
fun GameSession.getStatistics(player: Player): PlayerStatistics {
    // TODO("Not yet implemented")
    return PlayerStatistics(
        player,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0
    )
}
