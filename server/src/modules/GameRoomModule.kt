package org.keizar.server.modules

import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.keizar.game.BoardProperties
import org.keizar.server.modules.gameroom.GameRoom
import org.keizar.server.modules.gameroom.ServerGameRoomState
import org.keizar.server.modules.gameroom.PlayerSession
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.message.UserInfo
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.coroutines.CoroutineContext

interface GameRoomModule {
    fun createRoom(roomNumber: UInt, properties: BoardProperties)
    fun getRoom(roomNumber: UInt): GameRoom
    suspend fun joinRoom(roomNumber: UInt, userInfo: UserInfo)
    suspend fun connectToWebsocketSession(
        roomNumber: UInt,
        userInfo: UserInfo,
        session: DefaultWebSocketServerSession
    ): PlayerSession

    suspend fun suspendUntilGameEnds(playerSession: PlayerSession)
}

class GameRoomsModuleImpl(
    parentCoroutineContext: CoroutineContext,
    private val logger: Logger,
) : GameRoomModule {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))
    private val gameRooms: ConcurrentMap<UInt, GameRoom> = ConcurrentHashMap()

    override fun createRoom(roomNumber: UInt, properties: BoardProperties) {
        val room =
            GameRoom.create(roomNumber, properties, myCoroutineScope.coroutineContext, logger)
        if (gameRooms.putIfAbsent(roomNumber, room) != null) {
            // Room already created
            logger.info("Failure: room $roomNumber already created")
            throw BadRequestException("Room already created")
        }
        myCoroutineScope.launch {
            roomGarbageCollectionRoutine(room)
        }
    }

    override fun getRoom(roomNumber: UInt): GameRoom {
        val room = gameRooms[roomNumber]
        if (room == null) {
            // Room not found
            logger.info("Failure: room $roomNumber not found")
            throw NotFoundException("Room not found")
        }
        return room
    }

    override suspend fun joinRoom(roomNumber: UInt, userInfo: UserInfo) {
        val room = getRoom(roomNumber)
        if (!room.join(userInfo)) {
            logger.info("Failure: join room $roomNumber failed, possibly due to room full or game started")
            throw NotFoundException("Join room failed")
        }
    }

    override suspend fun connectToWebsocketSession(
        roomNumber: UInt,
        userInfo: UserInfo,
        session: DefaultWebSocketServerSession,
    ): PlayerSession {
        val room = gameRooms[roomNumber] ?: throw NotFoundException("Room $roomNumber not found")
        if (!room.containsPlayer(userInfo)) {
            throw BadRequestException("User ${userInfo.username} did not join room $roomNumber")
        }
        return room.connect(userInfo, session)
            ?: throw BadRequestException("Room $roomNumber connection to websocket failed")
    }

    override suspend fun suspendUntilGameEnds(playerSession: PlayerSession) {
        if (playerSession.state.value != PlayerSessionState.TERMINATING) {
            playerSession.state.first { it == PlayerSessionState.TERMINATING }
        }
    }

    private suspend fun roomGarbageCollectionRoutine(room: GameRoom) {
        room.state.first { it is ServerGameRoomState.Finished }
        logger.info("Destroying room ${room.roomNumber}")
        gameRooms.remove(room.roomNumber)
        room.close()
    }
}