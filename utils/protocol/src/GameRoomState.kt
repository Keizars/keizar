package org.keizar.utils.communication

import kotlinx.serialization.Serializable

@Serializable
enum class GameRoomState {
    STARTED, ALL_CONNECTED, PLAYING, FINISHED
}