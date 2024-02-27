package org.solvo.server.modules

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.*
import org.keizar.server.ServerContext
import org.keizar.utils.communication.account.AuthRequest
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.UsernameValidityResponse


val AuthDigest = getDigestFunction("SHA-256") { "ktor$it" }

fun Application.authenticationRouting(
    context: ServerContext,
) {
    val accounts = context.accounts

    routing {
        route("/users") {
            post("/register") {
                val request = call.receive<AuthRequest>()
                val username = request.username
                val hash = AuthDigest(request.password)

                val response = accounts.register(username, hash)
                call.respondAuth(response)
            }

            post("/available") {
                val username = call.request.queryParameters.getOrFail("username")

                val validity = !accounts.isUsernameTaken(username)
                call.respond(UsernameValidityResponse(validity))
            }

            post("/login") {
                val request = call.receive<AuthRequest>()
                val username = request.username
                val hash = AuthDigest(request.password)

                val response = accounts.login(username, hash)
                call.respondAuth(response)
            }
        }
    }
}

private suspend fun ApplicationCall.respondAuth(authResponse: AuthResponse) {
    if (authResponse.status == AuthStatus.SUCCESS) {
        respond(authResponse)
    } else {
        respond(HttpStatusCode.BadRequest, authResponse)
    }
}
