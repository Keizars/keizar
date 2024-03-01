package org.keizar.utils.communication.game

import kotlinx.serialization.Serializable

@Serializable
data class GameDataRequestData(
    val userName: String,
    val currentTimestamp: String)