package org.keizar.utils.communication.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.keizar.utils.communication.GameRoomState
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
data class PlayerStateChange(
    val username: String,
    val newState: PlayerSessionState
): Respond

@Serializable
data class RoomStateChange(
    val newState: GameRoomState
): Respond

@Serializable
data class RemoteSessionSetup(
    val playerAllocation: Player,
    val gameSnapshot: JsonElement,
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

@Serializable
data class ChangeBoard(
    val boardProperties: JsonElement
): Request

@Serializable
data object SetReady: Request