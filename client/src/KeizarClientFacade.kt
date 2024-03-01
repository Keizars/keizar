package org.keizar.client

import kotlinx.coroutines.flow.SharedFlow
import org.keizar.client.modules.GameRoomModule
import org.keizar.client.modules.GameRoomModuleImpl
import org.keizar.client.modules.KeizarHttpClient
import org.keizar.client.modules.KeizarHttpClientImpl
import org.keizar.game.RoomInfo
import kotlin.coroutines.CoroutineContext

class KeizarClientFacade(
    private val endpoint: String,
    private val clientToken: SharedFlow<String?>,
) {
    private val client: KeizarHttpClient = KeizarHttpClientImpl(endpoint)
    private val room: GameRoomModule = GameRoomModuleImpl(client, clientToken)

    /**
     * Get the room information of a specified room number.
     */
    suspend fun getRoom(roomNumber: UInt): RoomInfo {
        return room.getRoom(roomNumber)
    }

    /**
     * Create a room with random room number and join the room with given user info.
     * Return the room info.
     */
    suspend fun createRoomAndJoin(
        parentCoroutineContext: CoroutineContext,
    ): Boolean {
        val roomInfo = room.createRoom()
        return room.joinRoom(roomInfo.roomNumber)
    }

    /**
     * Join a room with given number and user info.
     * Return true on success.
     */
    suspend fun joinRoom(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext,
    ): Boolean {
        val roomInfo = room.getRoom(roomNumber)
        return room.joinRoom(roomInfo.roomNumber)
    }

    /**
     * Connect to server by websocket and create a game room session
     */
    suspend fun connect(
        roomInfo: RoomInfo,
        parentCoroutineContext: CoroutineContext,
    ): GameRoomClient? {
        if (!room.joinRoom(roomInfo.roomNumber)) return null
        return room.createClientRoom(roomInfo, parentCoroutineContext)
    }
}