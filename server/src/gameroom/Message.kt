package org.keizar.server.gameroom

import kotlinx.serialization.Serializable

@Serializable
sealed interface Message

@Serializable
sealed interface Request: Message

@Serializable
sealed interface Respond: Message

@Serializable
data class UserInfo(
    val username: String
): Request

@Serializable
data class StateChange(
    val newState: PlayerSessionState
): Respond

@Serializable
data object Exit : Request