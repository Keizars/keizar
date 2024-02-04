package org.keizar.utils.communication

import kotlinx.serialization.Serializable


@Serializable
enum class PlayerSessionState {
    STARTED, WAITING, PLAYING, TERMINATING
}