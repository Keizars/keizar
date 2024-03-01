package org.keizar.client

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import org.keizar.client.internal.KeizarHttpClient
import org.keizar.client.internal.KeizarHttpClientImpl
import kotlin.coroutines.CoroutineContext

class KeizarWebsocketClientFacade(
    private val endpoint: String,
    private val clientToken: SharedFlow<String?>,
) {
    private val client: KeizarHttpClient = KeizarHttpClientImpl(endpoint)

    /**
     * Connect to server by websocket and create a game room session
     */
    suspend fun connect(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext,
    ): GameRoomClient {
        val token = clientToken.first() ?: throw IllegalStateException("User token not available")
        if (!client.postRoomJoin(roomNumber, token)) {
            throw RoomFullException()
        }
        val websocketSession = client.getRoomWebsocketSession(roomNumber, token)
        return GameRoomClient.create(roomNumber, websocketSession, parentCoroutineContext).apply {
            start()
        }
    }
}


class RoomFullException : Exception("The room is full")