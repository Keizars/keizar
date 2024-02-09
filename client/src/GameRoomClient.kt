package org.keizar.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import org.keizar.game.BoardProperties
import kotlin.random.Random
import kotlin.random.nextUInt

data class GameRoom(
    val roomNumber: UInt,
    val gameProperties: BoardProperties,
)

interface GameRoomClient : AutoCloseable {
    suspend fun createRoom(roomNumber: UInt? = null, seed: Int? = null): GameRoom
    suspend fun createRoom(roomNumber: UInt? = null, boardProperties: BoardProperties): GameRoom
    suspend fun getRoom(roomNumber: UInt): GameRoom

    companion object {
        fun create(
            endpoint: String
        ): GameRoomClient {
            return GameRoomClientImpl(endpoint)
        }
    }
}

class GameRoomClientImpl(
    private val endpoint: String
) : GameRoomClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        Logging()
    }

    override suspend fun createRoom(roomNumber: UInt?, seed: Int?): GameRoom {
        val actualSeed = seed ?: Random.nextInt()
        val properties = BoardProperties.getStandardProperties(actualSeed)
        return createRoom(roomNumber, properties)
    }

    override suspend fun createRoom(roomNumber: UInt?, boardProperties: BoardProperties): GameRoom {
        val actualRoomNumber = roomNumber ?: Random.nextUInt(10000u, 99999u)
        val respond: HttpResponse = client.post(urlString = "$endpoint/room/create/$actualRoomNumber") {
            contentType(ContentType.Application.Json)
            setBody(boardProperties)
        }
        if (respond.status != HttpStatusCode.OK) {
            TODO("Handle exception")
        }
        return GameRoom(actualRoomNumber, boardProperties)
    }

    override suspend fun getRoom(roomNumber: UInt): GameRoom {
        val respond: HttpResponse = client.get(urlString = "$endpoint/room/get/$roomNumber")
        if (respond.status != HttpStatusCode.OK) {
            TODO("Handle exception")
        }
        return GameRoom(roomNumber, respond.body())
    }

    override fun close() {
        client.close()
    }
}
