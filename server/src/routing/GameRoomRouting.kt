package org.keizar.server.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.util.pipeline.PipelineContext
import org.keizar.game.BoardProperties
import org.keizar.game.RoomInfo
import org.keizar.server.ServerContext
import org.keizar.server.modules.gameroom.GameRoom
import org.keizar.server.modules.GameRoomModule
import org.keizar.server.modules.gameroom.PlayerSession
import org.keizar.server.utils.checkAuthentication
import org.keizar.server.utils.getAuthenticated
import org.keizar.server.utils.getUserId
import org.keizar.server.utils.postAuthenticated
import org.keizar.server.utils.websocketAuthenticated
import org.keizar.utils.communication.message.UserInfo

fun Application.gameRoomRouting(context: ServerContext) {
    val logger = log
    routing {
        val rooms: GameRoomModule = context.gameRooms
        websocketAuthenticated("/room/{roomNumber}") {
            val roomNumber: UInt = call.parameters["roomNumber"]?.toUIntOrNull()
                ?: throw BadRequestException("Invalid room number")
            val userId = getUserId() ?: return@websocketAuthenticated
            val username = context.accounts.getUser(userId)?.username
                ?: throw BadRequestException("Invalid user")
            val userInfo = UserInfo(username)

            logger.info("$username connecting to room $roomNumber websocket")
            val playerSession: PlayerSession
            try {
                playerSession = rooms.connectToWebsocketSession(roomNumber, userInfo, this)
            } catch (e: Exception) {
                logger.info("Failure: ${e.message}")
                throw e
            }
            logger.info("$username connected to room $roomNumber")

            rooms.suspendUntilGameEnds(playerSession)
            logger.info("$username exiting room $roomNumber")
        }

        postAuthenticated("/room/{roomNumber}/create") {
            val roomNumber: UInt = getRoomNumberOrBadRequest()
            if (!checkAuthentication()) return@postAuthenticated
            val properties = call.receive<BoardProperties>()

            logger.info("Creating room $roomNumber")
            rooms.createRoom(roomNumber, properties)
            logger.info("Room $roomNumber created")

            call.respond(HttpStatusCode.OK)
        }

        getAuthenticated("/room/{roomNumber}") {
            val roomNumber: UInt = getRoomNumberOrBadRequest()
            if (!checkAuthentication()) return@getAuthenticated

            logger.info("Fetching room $roomNumber")
            val room: GameRoom = rooms.getRoom(roomNumber)
            logger.info("Room $roomNumber fetch succeed")

            val info = RoomInfo(
                roomNumber = room.roomNumber,
                properties = room.properties,
                playerInfo = room.listPlayers(),
            )
            call.respond(info)
        }

        postAuthenticated("/room/{roomNumber}/join") {
            val roomNumber: UInt = getRoomNumberOrBadRequest()
            val userId = getUserId() ?: return@postAuthenticated
            val username = context.accounts.getUser(userId)?.username
                ?: throw BadRequestException("Invalid user")
            val userInfo = UserInfo(username)

            logger.info("User $username trying to join room $roomNumber")
            rooms.joinRoom(roomNumber, userInfo)
            logger.info("User $username joined room $roomNumber")

            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getRoomNumberOrBadRequest() =
    (call.parameters["roomNumber"]?.toUIntOrNull()
        ?: throw BadRequestException("Invalid room number"))
