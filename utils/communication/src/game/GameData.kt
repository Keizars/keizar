package org.keizar.utils.communication.game

import kotlinx.serialization.Serializable

@Serializable
data class GameData(
    val round1Statistics: RoundStats,
    val round2Statistics: RoundStats,
    val gameConfiguration: String,
    val user1: String,
    val user2: String,
    val currentTimestamp: String)