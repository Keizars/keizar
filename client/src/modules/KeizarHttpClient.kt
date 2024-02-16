package org.keizar.client.modules

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import org.keizar.client.exception.NetworkFailureException
import org.keizar.game.BoardProperties

interface KeizarHttpClient : AutoCloseable {
    suspend fun postRoomCreate(endpoint: String, actualRoomNumber: UInt, boardProperties: BoardProperties)
    suspend fun getRoom(roomNumber: UInt, endpoint: String): GameRoomInfo
}

class KeizarHttpClientImpl : KeizarHttpClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(ClientJson)
        }
        Logging()
    }

    override suspend fun postRoomCreate(
        endpoint: String,
        actualRoomNumber: UInt,
        boardProperties: BoardProperties
    ) {
        val respond: HttpResponse =
            client.post(urlString = "$endpoint/room/create/$actualRoomNumber") {
                contentType(ContentType.Application.Json)
                setBody(boardProperties)
            }
        if (respond.status != HttpStatusCode.OK) {
            throw NetworkFailureException("Failed postRoomCreate")
        }
    }

    override suspend fun getRoom(
        roomNumber: UInt,
        endpoint: String,
    ): GameRoomInfo {
        val respond: HttpResponse = client.get(urlString = "$endpoint/room/get/$roomNumber")
        if (respond.status != HttpStatusCode.OK) {
            throw NetworkFailureException("Failed getRoom")
        }
        return GameRoomInfo(roomNumber, respond.body())
    }

    override fun close() {
        client.close()
    }
}