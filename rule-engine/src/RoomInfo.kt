package org.keizar.game

import kotlinx.serialization.Serializable

@Serializable
data class RoomInfo(
    val properties: BoardProperties,
    val playerCount: Int,
    val playersReady: Boolean
)