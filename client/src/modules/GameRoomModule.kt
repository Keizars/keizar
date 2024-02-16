package org.keizar.client.modules

import org.keizar.game.BoardProperties
import kotlin.random.Random
import kotlin.random.nextUInt

data class GameRoomInfo(
    val roomNumber: UInt,
    val gameProperties: BoardProperties,
)

interface GameRoomModule {
    suspend fun createRoom(roomNumber: UInt? = null, seed: Int? = null): GameRoomInfo
    suspend fun createRoom(roomNumber: UInt? = null, boardProperties: BoardProperties): GameRoomInfo
    suspend fun getRoom(roomNumber: UInt): GameRoomInfo

    companion object {
        fun create(
            client: KeizarHttpClient,
        ): GameRoomModule {
            return GameRoomModuleImpl(client)
        }
    }
}

class GameRoomModuleImpl(
    private val client: KeizarHttpClient,
) : GameRoomModule {

    override suspend fun createRoom(roomNumber: UInt?, seed: Int?): GameRoomInfo {
        val actualSeed = seed ?: Random.nextInt()
        val properties = BoardProperties.getStandardProperties(actualSeed)
        return createRoom(roomNumber, properties)
    }

    override suspend fun createRoom(roomNumber: UInt?, boardProperties: BoardProperties): GameRoomInfo {
        val actualRoomNumber = roomNumber ?: Random.nextUInt(10000u, 99999u)
        client.postRoomCreate(actualRoomNumber, boardProperties)
        return GameRoomInfo(actualRoomNumber, boardProperties)
    }

    override suspend fun getRoom(roomNumber: UInt): GameRoomInfo {
        return client.getRoom(roomNumber)
    }
}
