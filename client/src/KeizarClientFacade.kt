package org.keizar.client

import kotlin.coroutines.CoroutineContext

class KeizarClientFacade(
    private val endpoint: String = "http://192.168.5.102:4392"
) {
    fun createRoomClient(): GameRoomClient {
        return GameRoomClient.create(endpoint)
    }

    suspend fun createGameSession(roomNumber: UInt, parentCoroutineContext: CoroutineContext): RemoteGameSession {
        val room = GameRoomClient.create(endpoint).getRoom(roomNumber)
        return RemoteGameSession.createAndConnect(room, parentCoroutineContext = parentCoroutineContext, endpoint)
    }
}