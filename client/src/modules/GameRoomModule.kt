package org.keizar.client.modules

import org.keizar.game.BoardProperties
import org.keizar.game.RoomInfo
import org.keizar.utils.communication.message.UserInfo
import kotlin.random.Random
import kotlin.random.nextUInt

interface GameRoomModule {
    suspend fun createRoom(roomNumber: UInt? = null, seed: Int? = null): RoomInfo
    suspend fun createRoom(roomNumber: UInt? = null, boardProperties: BoardProperties): RoomInfo
    suspend fun getRoom(roomNumber: UInt): RoomInfo
    suspend fun joinRoom(roomNumber: UInt, userInfo: UserInfo): Boolean
}

class GameRoomModuleImpl(
    private val client: KeizarHttpClient,
) : GameRoomModule {

    override suspend fun createRoom(roomNumber: UInt?, seed: Int?): RoomInfo {
        val actualSeed = seed ?: Random.nextInt()
        val properties = BoardProperties.getStandardProperties(actualSeed)
        return createRoom(roomNumber, properties)
    }

    override suspend fun createRoom(roomNumber: UInt?, boardProperties: BoardProperties): RoomInfo {
        val actualRoomNumber = roomNumber ?: Random.nextUInt(10000u, 99999u)
        client.postRoomCreate(actualRoomNumber, boardProperties)
        return RoomInfo(actualRoomNumber, boardProperties, 0, false)
    }

    override suspend fun getRoom(roomNumber: UInt): RoomInfo {
        return client.getRoom(roomNumber)
    }

    override suspend fun joinRoom(roomNumber: UInt, userInfo: UserInfo): Boolean {
        // TODO
        return true
    }
}
