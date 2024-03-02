package org.keizar.utils.communication.game

import kotlinx.serialization.Serializable

@Serializable
data class GameData(
    val round1Statistics: RoundStats,
    val round2Statistics: RoundStats,
    val gameConfiguration: String,
    val user1: String? = null,
    val user2: String? = null,
    val currentTimestamp: String,
    val userSaved: Boolean = false
)