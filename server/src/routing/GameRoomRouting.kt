package org.keizar.server.routing

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.keizar.game.BoardProperties
import org.keizar.game.RoomInfo
import org.keizar.server.gameroom.GameRoom
import org.keizar.server.gameroom.GameRoomImpl
import org.keizar.server.gameroom.PlayerSessionImpl
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.message.UserInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

fun Application.gameRoomRouting(parentScope: CoroutineScope) {
    val logger = log
    val parentContext = parentScope.coroutineContext
    routing {
        val gameRooms: ConcurrentMap<UInt, GameRoom> = ConcurrentHashMap()
        webSocket("/room/{roomNumber}") {
            val roomNumber: UInt = call.parameters["roomNumber"]?.toUIntOrNull()
                ?: throw BadRequestException("Invalid room number")
            val username: String = try {
                receiveDeserialized<UserInfo>().username
            } catch (e: WebsocketDeserializeException) {
                throw BadRequestException("Invalid UserInfo")
            }

            logger.info("$username connecting to room $roomNumber")

            val room = gameRooms[roomNumber] ?: throw NotFoundException("Room $roomNumber not found")
            val player = PlayerSessionImpl(this)
            if (!room.addPlayer(player)) {
                throw BadRequestException("Room $roomNumber is full")
            }

            logger.info("$username connected to room $roomNumber")

            if (player.state.value != PlayerSessionState.TERMINATING) {
                player.state.first { it == PlayerSessionState.TERMINATING }
            }
            logger.info("$username exiting room $roomNumber")

            if (room.finished.value) {
                logger.info("Destroying room $roomNumber")
                gameRooms.remove(roomNumber)
                room.close()
            }
        }

        post("/room/create/{roomNumber}") {
            val roomNumber: UInt = call.parameters["roomNumber"]?.toUIntOrNull()
                ?: throw BadRequestException("Invalid room number")

            val properties = call.receive<BoardProperties>()
            logger.info("Creating room $roomNumber")
            val room = GameRoomImpl(roomNumber, properties, parentContext, logger)
            if (gameRooms.putIfAbsent(roomNumber, room) != null) {
                // Room already created
                logger.info("Failure: room $roomNumber already created")
                throw BadRequestException("Room already created")
            }
            logger.info("Room $roomNumber created")
            call.respond(HttpStatusCode.OK)
        }

        get("/room/get/{roomNumber}") {
            val roomNumber: UInt = call.parameters["roomNumber"]?.toUIntOrNull()
                ?: throw BadRequestException("Invalid room number")

            logger.info("Fetching room $roomNumber")
            val room = gameRooms[roomNumber]
            if (room == null) {
                // Room not found
                logger.info("Failure: room $roomNumber not found")
                throw NotFoundException("Room not found")
            }
            logger.info("Room $roomNumber fetch succeed")
            val info = RoomInfo(
                roomNumber = room.roomNumber,
                properties = room.properties,
                playerCount = room.playerCount,
                playersReady = room.playersReady
            )
            call.respond(info)
        }

        post("/room/join/{roomNumber}") {
            val roomNumber: UInt = call.parameters["roomNumber"]?.toUIntOrNull()
                ?: throw BadRequestException("Invalid room number")

            // TODO
            call.respond(HttpStatusCode.OK)
        }
    }
}
