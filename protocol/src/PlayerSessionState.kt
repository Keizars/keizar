package org.keizar.utils.communication

import kotlinx.serialization.Serializable


@Serializable
enum class PlayerSessionState {
    DISCONNECTED, STARTED, READY, PLAYING, TERMINATING
}