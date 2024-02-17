package org.keizar.client

import org.keizar.client.modules.GameRoomModule
import org.keizar.client.modules.GameRoomModuleImpl
import org.keizar.client.modules.GameSessionModuleImpl
import org.keizar.client.modules.KeizarHttpClient
import org.keizar.client.modules.KeizarHttpClientImpl
import org.keizar.game.RoomInfo
import org.keizar.utils.communication.message.UserInfo
import kotlin.coroutines.CoroutineContext

class KeizarClientFacade(
    private val endpoint: String = "http://home.him188.moe:4392"
) {
    private val client: KeizarHttpClient = KeizarHttpClientImpl(endpoint)
    private val room: GameRoomModule = GameRoomModuleImpl(client)

    /**
     * Create a room with random room number.
     */
    suspend fun createRoom(): RoomInfo {
        return room.createRoom()
    }

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
        userInfo: UserInfo
    ): RoomInfo {
        val roomInfo = room.createRoom()
        joinRoom(roomInfo, userInfo)
        return roomInfo
    }

    /**
     * Join a room with given number and user info.
     * Return true on success.
     */
    suspend fun joinRoom(
        roomNumber: UInt,
        userInfo: UserInfo,
    ): Boolean {
        val roomInfo = room.getRoom(roomNumber)
        return joinRoom(roomInfo, userInfo)
    }

    private suspend fun joinRoom(
        roomInfo: RoomInfo,
        userInfo: UserInfo,
    ): Boolean {
        return room.joinRoom(roomInfo.roomNumber, userInfo)
    }

    suspend fun reconnectToRoom(
        roomNumber: UInt,
        userInfo: UserInfo,
    ): RemoteGameSession {
        TODO()
    }

    /**
     * Create and return a RemoteGameSession for the room specified by its room number.
     * Should be called only when the players in the room are ready to start.
     */
    suspend fun createGameSession(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext,
        userInfo: UserInfo,
    ): RemoteGameSession {
        val roomInfo = room.getRoom(roomNumber)
        val session = GameSessionModuleImpl(roomInfo.roomNumber, parentCoroutineContext, client)
        return RemoteGameSession.createAndConnect(
            parentCoroutineContext,
            session,
            roomInfo.properties,
            userInfo,
        )
    }
}