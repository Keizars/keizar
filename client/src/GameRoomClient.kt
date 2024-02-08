package org.keizar.client

import io.ktor.client.HttpClient
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
    suspend fun getRoom(roomNumber: UInt?): GameRoom

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
    private val client = HttpClient()

    override suspend fun createRoom(roomNumber: UInt?, seed: Int?): GameRoom {
        val actualRoomNumber = roomNumber ?: Random.nextUInt()
        val actualSeed = seed ?: Random.nextInt()
        // TODO: client post
        return GameRoom(actualRoomNumber, BoardProperties.getStandardProperties(actualSeed))
    }

    override suspend fun createRoom(roomNumber: UInt?, boardProperties: BoardProperties): GameRoom {
        val actualRoomNumber = roomNumber ?: Random.nextUInt()
        // TODO: client post
        return GameRoom(actualRoomNumber, boardProperties)
    }

    override suspend fun getRoom(roomNumber: UInt?): GameRoom {
        TODO("client get")
    }

    override fun close() {
        client.close()
    }
}
