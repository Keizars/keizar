package org.keizar.server.routing

import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.routing.*
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.webSocket
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.flow.first
import org.keizar.server.gameroom.GameRoom
import org.keizar.server.gameroom.GameRoomImpl
import org.keizar.server.gameroom.PlayerSessionImpl
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.message.UserInfo

fun Application.gameRoomRouting() {
    val logger = log
    routing {
        val gameRooms: ConcurrentMap<UInt, GameRoom> = ConcurrentMap()
        webSocket("/room/{roomNumber}") {
            val roomNumber: UInt = call.parameters["roomNumber"]?.toUIntOrNull()
                ?: throw BadRequestException("Invalid room number")
            val username: String = try {
                receiveDeserialized<UserInfo>().username
            } catch (e: WebsocketDeserializeException) {
                throw BadRequestException("Invalid UserInfo")
            }

            logger.info("$username connecting to room $roomNumber")

            val room = gameRooms.getOrPut(roomNumber) {
                logger.info("Creating room $roomNumber")
                GameRoomImpl(roomNumber, this.coroutineContext)
            }
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
                room.close()
            }
        }
    }
}
