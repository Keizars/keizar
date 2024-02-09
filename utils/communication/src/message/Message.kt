package org.keizar.utils.communication.message

import kotlinx.serialization.Serializable
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Player


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
data class PlayerAllocation(
    val who: Player
): Respond

@Serializable
data object Exit : Request

@Serializable
data object ConfirmNextRound: Request, Respond

@Serializable
data class Move(
    val from: BoardPos,
    val to: BoardPos,
): Request, Respond