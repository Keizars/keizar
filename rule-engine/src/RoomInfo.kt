package org.keizar.game

import kotlinx.serialization.Serializable
import org.keizar.utils.communication.message.UserInfo

@Serializable
data class RoomInfo(
    val roomNumber: UInt,
    val properties: BoardProperties,
    /**
     * Information of players currently in the room
     */
    val playerInfo: List<PlayerInfo>,
)

@Serializable
data class PlayerInfo(
    val user: UserInfo,
    val isHost: Boolean,
    val isReady: Boolean,
)