package org.keizar.client

import org.keizar.client.modules.GameRoomInfo
import org.keizar.client.modules.GameRoomModule
import org.keizar.client.modules.KeizarHttpClient
import org.keizar.client.modules.KeizarHttpClientImpl
import org.keizar.utils.communication.message.UserInfo
import kotlin.coroutines.CoroutineContext

class KeizarClientFacade(
    private val endpoint: String = "http://home.him188.moe:4392"
) {
    private val client: KeizarHttpClient = KeizarHttpClientImpl(endpoint)
    private val room: GameRoomModule = GameRoomModule.create(client)

    suspend fun createRoom(): GameRoomInfo {
        return room.createRoom()
    }

    suspend fun createRoomAndJoin(userInfo: UserInfo): GameRoomInfo {
        val roomInfo = room.createRoom()
        room.joinRoom(roomInfo.roomNumber, userInfo)
        return roomInfo
    }

    suspend fun createGameSession(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext
    ): RemoteGameSession {
        val room = room.getRoom(roomNumber)
        return RemoteGameSession.createAndConnect(
            room,
            parentCoroutineContext = parentCoroutineContext,
            client,
        )
    }
}