package org.keizar.client

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import org.keizar.client.exception.RoomFullException
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
     * @throws RoomFullException if the join room failed
     */
    suspend fun connect(
        roomNumber: UInt,
        parentCoroutineContext: CoroutineContext,
    ): Room {
        val token = clientToken.first() ?: throw IllegalStateException("User token not available")
        if (!client.postRoomJoin(roomNumber, token)) {
            throw RoomFullException()
        }
        val self = client.getSelf(token)
        val websocketSession = client.getRoomWebsocketSession(roomNumber, token)
        val roomInfo = client.getRoom(roomNumber, token)
        return Room.create(
            self = self,
            roomInfo = roomInfo,
            websocketSession = websocketSession,
            parentCoroutineContext = parentCoroutineContext
        )
    }
}


