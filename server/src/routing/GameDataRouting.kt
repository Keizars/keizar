package org.keizar.server.routing

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.keizar.server.ServerContext
import org.keizar.server.utils.deleteAuthenticated
import org.keizar.server.utils.getAuthenticated
import org.keizar.server.utils.getUserIdOrRespond
import org.keizar.server.utils.postAuthenticated
import org.keizar.utils.communication.game.GameDataId
import org.keizar.utils.communication.game.GameDataResponse
import org.keizar.utils.communication.game.GameDataStore
import java.util.UUID

fun Application.gameDataRouting(context: ServerContext) {
    val gameDataTable = context.gameData

    routing {
        route("/games") {
            post{
                val gameData = call.receive<GameDataStore>()
                val id = gameDataTable.addGameData(gameData)
                call.respond(GameDataId(id))
            }

            getAuthenticated {
                val userId = getUserIdOrRespond() ?: return@getAuthenticated
                call.respond(gameDataTable.getGameDataByUsedID(userId))
            }

            postAuthenticated ( "/save/" ) {
                call.respond(GameDataResponse(false))
            }

            postAuthenticated ( "/save/{dataId}" ) {
                val dataId = call.parameters["dataId"] ?: return@postAuthenticated
                gameDataTable.saveGameData(UUID.fromString(dataId))
                call.respond(GameDataResponse(true))
            }


            deleteAuthenticated("/{id}") {
                val userId = getUserIdOrRespond() ?: return@deleteAuthenticated
                val id = call.parameters["id"] ?: return@deleteAuthenticated
                val success = gameDataTable.removeGameData(UUID.fromString(id))
                call.respond(GameDataResponse(success))
            }
        }
    }
}