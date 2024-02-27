package org.keizar.server.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext
import java.util.UUID

@KtorDsl
inline fun Application.routeAuthenticated(crossinline block: Routing.() -> Unit) {
    routing {
        authenticate("auth-bearer") {
            this@routing.block()
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkUserId(): UUID? {
    val uidStr = call.principal<UserIdPrincipal>()?.name
    if (uidStr == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return null
    }
    return uidStr.formatToUuidOrNull()
}

suspend fun WebSocketServerSession.checkUserId(): UUID? {
    val uidStr = call.principal<UserIdPrincipal>()?.name
    if (uidStr == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return null
    }
    return uidStr.formatToUuidOrNull()
}