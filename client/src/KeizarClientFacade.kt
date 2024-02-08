package org.keizar.client

import org.keizar.game.BoardProperties
import kotlin.coroutines.CoroutineContext

class KeizarClientFacade(
    private val endpoint: String = "http://home.him188.moe:4392"
) {
    fun createRoomClient(): GameRoomClient {
        return GameRoomClient.create(endpoint)
    }

    suspend fun createGameSession(roomNumber: UInt, parentCoroutineContext: CoroutineContext): RemoteGameSession {
        val room = GameRoom(roomNumber, BoardProperties.getStandardProperties())
        return RemoteGameSession.createAndConnect(room, parentCoroutineContext = parentCoroutineContext, endpoint)
    }
}