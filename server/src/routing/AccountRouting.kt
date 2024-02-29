package org.keizar.server.routing

import io.ktor.client.content.LocalFileContent
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveStream
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import org.keizar.server.ServerContext
import org.keizar.server.utils.getUserId
import org.keizar.utils.communication.account.ImageUrlExchange
import java.util.UUID

fun Application.accountRouting(context: ServerContext) {
    val accounts = context.accounts

    routing {
        route("/users") {
            authenticate("auth-bearer") {
                get("me") {
                    val uid = getUserId() ?: return@get
                    val user = accounts.getUser(uid) ?: throw NotFoundException("Invalid user")
                    call.respond(user)
                }
                post("/avatar") {
                    val uid = getUserId() ?: return@post
                    val contentType = call.request.contentType()
                    val path = call.receiveStream().use { input ->
                        accounts.uploadNewAvatar(uid, input, contentType)
                    }
                    call.respond(ImageUrlExchange(path))
                }
            }
            get("/search") {
                val name: String = call.request.queryParameters.getOrFail("name")
                val user = accounts.getUserByName(name) ?: throw NotFoundException("Invalid user")
                call.respond(user)
            }
            get("/{uid}/avatar") {
                val uid = try {
                    UUID.fromString(call.parameters.getOrFail("uid"))
                } catch (e: IllegalArgumentException) {
                    throw NotFoundException("Invalid UserId")
                }

                val (avatar, contentType) = accounts.getUserAvatar(uid) ?: kotlin.run {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(LocalFileContent(avatar, contentType))
            }
        }
    }
}