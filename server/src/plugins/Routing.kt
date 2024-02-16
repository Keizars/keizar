package org.keizar.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import org.keizar.server.routing.gameRoomRouting

fun Application.configureMultiplayerRouting(parentScope: CoroutineScope) {
    routing {
        get("/status") {
            call.respondText("Server is running 2")
        }
    }
    gameRoomRouting(parentScope)
}
