package org.keizar.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.keizar.server.ServerContext
import org.keizar.server.routing.accountRouting
import org.keizar.server.routing.gameRoomRouting
import org.solvo.server.modules.authenticationRouting

fun Application.configureMultiplayerRouting(context: ServerContext) {
    routing {
        get("/status") {
            call.respondText("Server is running 2")
        }
    }
    gameRoomRouting(context)
    authenticationRouting(context)
    accountRouting(context)
}
