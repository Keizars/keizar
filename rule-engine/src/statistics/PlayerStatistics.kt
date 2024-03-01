package org.keizar.game.statistics

import org.keizar.game.GameSession
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.game.PlayerStatistics

/**
 * TODO: add comments
 */
fun GameSession.getStatistics(player: Player): PlayerStatistics {
    val round1Time = 0
    val round2Time = 0
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