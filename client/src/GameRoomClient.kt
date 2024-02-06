package org.keizar.client

import org.keizar.game.BoardProperties
import io.ktor.client.*
import kotlin.random.Random
import kotlin.random.nextUInt

data class GameRoom(
    val roomNumber: UInt,
    val gameProperties: BoardProperties,
)

interface GameRoomClient {
    suspend fun createRoom(roomNumber: UInt? = null, seed: Int? = null): GameRoom
    suspend fun createRoom(roomNumber: UInt? = null, boardProperties: BoardProperties): GameRoom

    companion object {
        fun create(): GameRoomClient {
            return GameRoomClientImpl()
        }
    }
}

class GameRoomClientImpl: GameRoomClient {
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
}
