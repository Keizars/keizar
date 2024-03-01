package org.keizar.client

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import org.keizar.client.internal.KeizarHttpClient
import org.keizar.client.internal.KeizarHttpClientImpl
import org.keizar.game.RoomInfo
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
        roomInfo: RoomInfo,
        parentCoroutineContext: CoroutineContext,
    ): GameRoomClient? {
        val token = clientToken.first() ?: throw IllegalStateException("User token not available")
        if (!client.postRoomJoin(roomInfo.roomNumber, token)) return null
        val websocketSession = client.getRoomWebsocketSession(roomInfo.roomNumber, token)
        return GameRoomClient.create(roomInfo, websocketSession, parentCoroutineContext).apply {
            start()
        }
    }
}