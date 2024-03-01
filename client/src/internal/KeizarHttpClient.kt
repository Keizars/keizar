package org.keizar.client.internal

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.keizar.client.exception.NetworkFailureException
import org.keizar.game.BoardProperties
import org.keizar.game.RoomInfo
import org.keizar.utils.communication.CommunicationModule
import org.keizar.utils.communication.message.UserInfo

private val ClientJson = Json {
    ignoreUnknownKeys = true
    serializersModule = CommunicationModule
}

internal interface KeizarHttpClient : AutoCloseable {
    suspend fun getRoom(roomNumber: UInt, token: String): RoomInfo
    suspend fun postRoomJoin(roomNumber: UInt, token: String): Boolean
    suspend fun getRoomWebsocketSession(
        roomNumber: UInt,
        token: String,
    ): DefaultClientWebSocketSession

}

internal class KeizarHttpClientImpl(
    private val endpoint: String,
) : KeizarHttpClient {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(ClientJson)
        }
        Logging {
            level = LogLevel.INFO
        }
    }

    override suspend fun getRoom(
        roomNumber: UInt,
        token: String,
    ): RoomInfo {
        val respond: HttpResponse = client.get(urlString = "$endpoint/room/$roomNumber") {
            header("Authorization", "Bearer $token")
        }
        if (respond.status != HttpStatusCode.OK) {
            throw NetworkFailureException("Failed getRoom")
        }
        return respond.body<RoomInfo>()
    }

    override suspend fun postRoomJoin(
        roomNumber: UInt,
        token: String,
    ): Boolean {
        val respond = client.post(
            urlString = "$endpoint/room/$roomNumber/join"
        ) {
            header("Authorization", "Bearer $token")
        }
        return respond.status == HttpStatusCode.OK
    }

    override suspend fun getRoomWebsocketSession(
        roomNumber: UInt,
        token: String,
    ): DefaultClientWebSocketSession {
        return client.webSocketSession(
            urlString = "ws:${endpoint.substringAfter(':')}/room/$roomNumber",
        ) {
            header("Authorization", "Bearer $token")
        }
    }

    override fun close() {
        client.close()
    }
}