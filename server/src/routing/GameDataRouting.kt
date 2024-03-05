package org.keizar.server.routing

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.keizar.server.ServerContext
import org.keizar.server.utils.deleteAuthenticated
import org.keizar.server.utils.getAuthenticated
import org.keizar.server.utils.getUserIdOrRespond
import org.keizar.utils.communication.game.GameDataStore
import java.util.UUID

fun Application.gameDataRouting(context: ServerContext) {
    val gameDataTable = context.gameData

    routing {
        route("/games") {
            post{
                val gameData = call.receive<GameDataStore>()
                gameDataTable.addGameData(gameData)
            }

            getAuthenticated {
                val userId = getUserIdOrRespond() ?: return@getAuthenticated
                call.respond(gameDataTable.getGameDataByUsedID(userId))
            }


            deleteAuthenticated("/{id}") {
                val userId = getUserIdOrRespond() ?: return@deleteAuthenticated
                val id = call.parameters["id"] ?: return@deleteAuthenticated
                gameDataTable.removeGameData(UUID.fromString(id))
            }
        }
    }
}