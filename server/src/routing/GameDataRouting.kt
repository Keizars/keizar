package org.keizar.server.routing

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.keizar.server.ServerContext
import org.keizar.server.utils.getUserId
import org.keizar.utils.communication.game.GameDataStore
import java.util.UUID

fun Application.gameDataRouting(context: ServerContext) {
    val gameDataTable = context.gameData

    routing {
        route("/games") {
            authenticate("auth-bearer") {
                post{
                    val gameData = call.receive<GameDataStore>()
                    gameDataTable.addGameData(gameData)
                }

                get {
                    val userId = getUserId() ?: return@get
                    call.respond(gameDataTable.getGameDataByUsedID(userId))
                }


                delete("/{id}") {
                    val id = call.parameters["id"] ?: return@delete
                    gameDataTable.removeGameData(UUID.fromString(id))
                }
            }
        }
    }
}