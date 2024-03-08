package org.keizar.server.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.util.KtorDsl
import io.ktor.util.pipeline.PipelineContext
import java.util.UUID

/**
 * Shortcut functions for `authenticate("auth-bearer") { <routingMethod>("path") { ... } }`.
 */

@KtorDsl
inline fun Route.getAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer", optional = optional) {
        get(path) { block() }
    }
}

@KtorDsl
inline fun Route.postAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer", optional = optional) {
        post(path) { block() }
    }
}

@KtorDsl
inline fun Route.putAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer", optional = optional) {
        put(path) { block() }
    }
}

@KtorDsl
inline fun Route.patchAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer", optional = optional) {
        patch(path) { block() }
    }
}

@KtorDsl
inline fun Route.deleteAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
) {
    authenticate("auth-bearer", optional = optional) {
        delete(path) { block() }
    }
}

@KtorDsl
inline fun Route.websocketAuthenticated(
    path: String = "",
    optional: Boolean = false,
    crossinline block: suspend DefaultWebSocketServerSession.() -> Unit
) {
    authenticate("auth-bearer", optional = optional) {
        webSocket(path) { block() }
    }
}

/**
 * Returns the user id if the user is authenticated, otherwise responds with [HttpStatusCode.Unauthorized] and returns `null`.
 */
suspend fun PipelineContext<Unit, ApplicationCall>.getUserIdOrRespond(): UUID? {
    val uid = call.principal<UserIdPrincipal>()?.name?.formatToUuidOrNull()
    if (uid == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return null
    }
    return uid
}

/**
 * Returns the user id if the user is authenticated, otherwise returns `null`.
 * Does not respond to the client.
 */
fun PipelineContext<Unit, ApplicationCall>.getUserIdOrNull(): UUID? {
    return call.principal<UserIdPrincipal>()?.name?.formatToUuidOrNull()
}

/**
 * Checks if the user is authenticated, and responds with [HttpStatusCode.Unauthorized] if not.
 * Returns `true` if the user is authenticated, `false` otherwise.
 */
suspend fun PipelineContext<Unit, ApplicationCall>.checkAuthentication(): Boolean {
    return (call.principal<UserIdPrincipal>() != null).also {
        if (!it) call.respond(HttpStatusCode.Unauthorized)
    }
}

/**
 * Returns the user id if the user is authenticated, 
 * otherwise responds with [HttpStatusCode.Unauthorized] and returns `null`.
 */
suspend fun WebSocketServerSession.getUserIdOrRespond(): UUID? {
    val uidStr = call.principal<UserIdPrincipal>()?.name
    if (uidStr == null) {
        call.respond(HttpStatusCode.Unauthorized)
        return null
    }
    return uidStr.formatToUuidOrNull()
}