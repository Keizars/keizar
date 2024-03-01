package org.keizar.utils.communication.game

import kotlinx.serialization.Serializable

@Serializable
data class GameData(
    val round1Statistics: RoundStatistics,
    val round2Statistics: RoundStatistics,
    val gameConfiguration: String,
    val opponentName: String,
    val userName: String,
    val currentTimestamp: String)