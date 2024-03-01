package org.keizar.game.statistics

import kotlinx.serialization.Serializable
import org.keizar.game.GameSession
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.game.PlayerStatistics

/**
 * TODO: add comments
 */
fun GameSession.getStatistics(player: Player): PlayerStatistics {
    val round1Time = getRoundTime(0)  ?: -1
    val round2Time = getRoundTime(1)  ?: -1
    return PlayerStatistics(
        player,
        0,
        0,
        0,
        0,
        0,
        0,
        round1Time,
        round2Time,
        0,
        0
    )
}
