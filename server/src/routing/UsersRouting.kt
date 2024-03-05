package org.keizar.server.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveStream
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import org.keizar.server.ServerContext
import org.keizar.server.utils.getAuthenticated
import org.keizar.server.utils.getUserIdOrRespond
import org.keizar.server.utils.patchAuthenticated
import org.keizar.server.utils.postAuthenticated
import org.keizar.server.utils.putAuthenticated
import org.keizar.utils.communication.account.ChangePasswordRequest
import org.keizar.utils.communication.account.ChangePasswordResponse
import org.keizar.utils.communication.account.EditUserRequest
import org.keizar.utils.communication.account.EditUserResponse
import org.keizar.utils.communication.account.User
import org.keizar.utils.communication.account.UsernameValidityResponse
import org.solvo.server.modules.AuthDigest

fun Application.usersRouting(context: ServerContext) {
    val accounts = context.accounts

    routing {
        route("/users") {
            getAuthenticated("/me") {
                val uid = getUserIdOrRespond() ?: return@getAuthenticated
                val user: User? = accounts.getUser(uid)
                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@getAuthenticated
                }
                call.respond(user)
            }
            putAuthenticated("/avatar") {
                val uid = getUserIdOrRespond() ?: return@putAuthenticated
                val contentType = call.request.contentType()
//                    call.receiveMultipart().readAllParts().first().let { part ->
//                        accounts.uploadNewAvatar(
//                            uid, when (part) {
//                                is PartData.BinaryChannelItem -> part.provider().toInputStream()
//                                is PartData.BinaryItem -> part.provider().readBytes().inputStream()
//                                is PartData.FileItem -> part.provider().readBytes().inputStream()
//                                is PartData.FormItem -> part.value.toByteArray().inputStream()
//                            }, contentType
//                        )
//                    }
                call.receiveStream().use { input ->
                    accounts.uploadNewAvatar(uid, input, contentType)
                }

                call.respond(HttpStatusCode.OK)
            }
            patchAuthenticated("/me") {
                val uid = getUserIdOrRespond() ?: return@patchAuthenticated
                val request = call.receive<EditUserRequest>()
                accounts.updateInfo(
                    uid,
                    newUsername = request.username,
                    newNickname = request.nickname,
                )
                call.respond(EditUserResponse(success = true))
            }
            postAuthenticated("/me/password") {
                val uid = getUserIdOrRespond() ?: return@postAuthenticated
                val request = call.receive<ChangePasswordRequest>()
                val hash = AuthDigest(request.password)
                accounts.updateInfo(uid, passwordHash = hash.toString(Charsets.UTF_8))
                call.respond(ChangePasswordResponse(success = true))
            }
            get("/{username}") {
                val username = call.parameters.getOrFail("username")
                val user =
                    accounts.getUserByUsername(username) ?: throw NotFoundException("No such user")
                call.respond(user)
            }
            get("/search") {
                val name: String = call.request.queryParameters.getOrFail("name")
                val user =
                    accounts.getUserByUsername(name) ?: throw NotFoundException("Invalid user")
                call.respond(user)
            }
            post("/available") {
                val username = call.request.queryParameters.getOrFail("username")
                val available = !accounts.isUsernameTaken(username)
                call.respond(UsernameValidityResponse(available))
            }
//            get("/{uid}/avatar") {
//                val uid = try {
//                    UUID.fromString(call.parameters.getOrFail("uid"))
//                } catch (e: IllegalArgumentException) {
//                    throw NotFoundException("Invalid UserId")
//                }
//
//                val (avatar, contentType) = accounts.getUserAvatar(uid) ?: kotlin.run {
//                    call.respond(HttpStatusCode.NotFound)
//                    return@get
//                }
//                call.respond(LocalFileContent(avatar, contentType))
//            }
        }
    }
}