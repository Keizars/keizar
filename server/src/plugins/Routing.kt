package org.keizar.server.plugins

import io.ktor.server.application.*
import org.keizar.server.routing.gameRoomRouting

fun Application.configureRouting() {
    gameRoomRouting()
}
