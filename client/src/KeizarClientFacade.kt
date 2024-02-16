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

    suspend fun createRoom(): RoomInfo {
        return room.createRoom()
    }

    suspend fun getRoom(roomNumber: UInt): RoomInfo {
        return room.getRoom(roomNumber)
    }

    suspend fun createRoomAndJoin(
        parentCoroutineContext: CoroutineContext,
        userInfo: UserInfo
    ): RemoteGameSession {
        val roomInfo = room.createRoom()
        return joinRoom(roomInfo, parentCoroutineContext, userInfo)
    }

    suspend fun joinRoom(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext,
        userInfo: UserInfo = UserInfo("TODO: temp"),
    ): RemoteGameSession {
        val roomInfo = room.getRoom(roomNumber)
        return joinRoom(roomInfo, parentCoroutineContext, userInfo)
    }

    private suspend fun joinRoom(
        roomInfo: RoomInfo,
        parentCoroutineContext: CoroutineContext,
        userInfo: UserInfo,
    ): RemoteGameSession {
        room.joinRoom(roomInfo.roomNumber, userInfo)
        return createGameSession(roomInfo, parentCoroutineContext, userInfo)
    }

    suspend fun reconnectToRoom(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext,
        userInfo: UserInfo,
    ): RemoteGameSession {
        val roomInfo = room.getRoom(roomNumber)
        return createGameSession(roomInfo, parentCoroutineContext, userInfo)
    }

    private suspend fun createGameSession(
        roomInfo: RoomInfo,
        parentCoroutineContext: CoroutineContext,
        userInfo: UserInfo,
    ): RemoteGameSession {
        val session = GameSessionModuleImpl(roomInfo.roomNumber, parentCoroutineContext, client)
        return RemoteGameSession.createAndConnect(
            parentCoroutineContext,
            session,
            roomInfo.properties,
            userInfo,
        )
    }
}