package org.keizar.client.modules

import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
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
    private companion object {
        private val logger = logger<GameRoomModuleImpl>()
    }

    override suspend fun createRoom(roomNumber: UInt?, seed: Int?): RoomInfo {
        val actualSeed = seed ?: Random.nextInt()
        val properties = BoardProperties.getStandardProperties(actualSeed)
        return createRoom(roomNumber, properties)
    }

    override suspend fun createRoom(roomNumber: UInt?, boardProperties: BoardProperties): RoomInfo {
        val actualRoomNumber = roomNumber ?: Random.nextUInt(10000u, 99999u)
        runCatching {
            client.postRoomCreate(actualRoomNumber, boardProperties)
        }.onSuccess {
            logger.info { "GameRoomModule.createRoom: successfully created $actualRoomNumber" }
        }.onFailure {
            logger.error(it) { "GameRoomModule.createRoom: failed to create $actualRoomNumber" }
        }.getOrThrow()
        return RoomInfo(actualRoomNumber, boardProperties, 0, false)
    }

    override suspend fun getRoom(roomNumber: UInt): RoomInfo {
        return client.getRoom(roomNumber)
    }

    override suspend fun joinRoom(roomNumber: UInt, userInfo: UserInfo): Boolean {
        return kotlin.runCatching { client.postRoomJoin(roomNumber, userInfo) }
            .onSuccess {
                logger.info { "GameRoomModule.joinRoom: successfully joined $roomNumber" }
            }.onFailure {
                logger.error(it) { "GameRoomModule.joinRoom: failed to join $roomNumber" }
            }.getOrThrow()
    }
}
