package org.keizar.client

import org.keizar.client.modules.GameRoomModule
import org.keizar.client.modules.KeizarHttpClient
import org.keizar.client.modules.KeizarHttpClientImpl
import kotlin.coroutines.CoroutineContext

class KeizarClientFacade(
    private val endpoint: String = "http://home.him188.moe:4392"
) {
    private val client: KeizarHttpClient = KeizarHttpClientImpl(endpoint)

    fun createRoomClient(): GameRoomModule {
        return GameRoomModule.create(client)
    }

    suspend fun createGameSession(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext
    ): RemoteGameSession {
        val room = GameRoomModule.create(client).getRoom(roomNumber)
        return RemoteGameSession.createAndConnect(
            room,
            parentCoroutineContext = parentCoroutineContext,
            client,
        )
    }
}