package org.keizar.server.routing

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.flow.first
import org.keizar.game.BoardProperties
import org.keizar.server.gameroom.GameRoom
import org.keizar.server.gameroom.GameRoomImpl
import org.keizar.server.gameroom.PlayerSessionImpl
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.message.UserInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

fun Application.gameRoomRouting() {
    val logger = log
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
            val room = GameRoomImpl(roomNumber, properties, coroutineContext)
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
            call.respond(room.properties)
        }
    }
}
