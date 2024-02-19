package org.keizar.game

import kotlinx.serialization.Serializable

@Serializable
data class RoomInfo(
    val roomNumber: UInt,
    val properties: BoardProperties,
    /**
     * Number of players currently in the room
     */
    val playerCount: Int,
    /**
     * Whether all players are ready to start the game.
     */
    val playersReady: Boolean
)