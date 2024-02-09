package org.keizar.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.keizar.server.routing.gameRoomRouting

fun Application.configureMultiplayerRouting() {
    routing {
        get("/status") {
            call.respondText("Server is running")
        }
    }
    gameRoomRouting()
}
