package org.keizar.server.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext
import java.util.UUID

@KtorDsl
inline fun Route.getAuthenticated(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer") {
        get(path) { block() }
    }
}

@KtorDsl
inline fun Route.postAuthenticated(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer") {
        post(path) { block() }
    }
}

@KtorDsl
inline fun Route.putAuthenticated(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer") {
        put(path) { block() }
    }
}

@KtorDsl
inline fun Route.patchAuthenticated(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer") {
        patch(path) { block() }
    }
}

@KtorDsl
inline fun Route.deleteAuthenticated(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer") {
        delete(path) { block() }
    }
}

@KtorDsl
inline fun Route.websocketAuthenticated(
    path: String = "",
    crossinline block: suspend DefaultWebSocketServerSession.() -> Unit
) {
    authenticate("auth-bearer") {
        webSocket(path) { block() }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.getUserId(): UUID? {
    val uid = call.principal<UserIdPrincipal>()?.name?.formatToUuidOrNull()
    if (uid == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return null
    }
    return uid
}

suspend fun PipelineContext<Unit, ApplicationCall>.checkAuthentication(): Boolean {
    return (call.principal<UserIdPrincipal>() != null).also {
        if (!it) call.respond(HttpStatusCode.Unauthorized)
    }
}

suspend fun WebSocketServerSession.getUserId(): UUID? {
    val uidStr = call.principal<UserIdPrincipal>()?.name
    if (uidStr == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return null
    }
    return uidStr.formatToUuidOrNull()
}