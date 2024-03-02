package org.keizar.utils.communication.game

import kotlinx.serialization.Serializable

@Serializable
data class GameData(
    val round1Statistics: RoundStats,
    val round2Statistics: RoundStats,
    val gameConfiguration: String,
    val opponentName: String,
    val userName: String,
    val currentTimestamp: String)