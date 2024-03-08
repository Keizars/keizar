package org.keizar.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.keizar.server.ServerContext
import org.keizar.server.routing.gameDataRouting
import org.keizar.server.routing.usersRouting
import org.keizar.server.routing.gameRoomRouting
import org.keizar.server.routing.seedBankRouting
import org.keizar.server.routing.authenticationRouting

fun Application.configureMultiplayerRouting(context: ServerContext) {
    routing {
        get("/status") {
            call.respondText("Server is running 2")
        }
    }
    gameRoomRouting(context)
    authenticationRouting(context)
    usersRouting(context)
    seedBankRouting(context)
    gameDataRouting(context)
}
