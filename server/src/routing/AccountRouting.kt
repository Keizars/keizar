package org.keizar.server.routing

import io.ktor.client.content.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.keizar.server.ServerContext
import org.keizar.server.utils.getUserId
import org.keizar.utils.communication.account.ImageUrlExchange
import java.util.*

fun Application.accountRouting(context: ServerContext) {
    val accounts = context.accounts

    routing {
        route("/account") {
            authenticate("auth-bearer") {
                get("/me") {
                    val uid = getUserId() ?: return@get
                    val user = accounts.getUser(uid) ?: throw NotFoundException("Invalid user")
                    call.respond(user)
                }
                post("/newAvatar") {
                    val uid = getUserId() ?: return@post
                    val contentType = call.request.contentType()
                    val path = call.receiveStream().use { input ->
                        accounts.uploadNewAvatar(uid, input, contentType)
                    }
                    call.respond(ImageUrlExchange(path))
                }
            }
            get("search") {
                val name: String = call.parameters.getOrFail("name") // currently required
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