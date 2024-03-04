package org.keizar.server.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.keizar.server.ServerContext
import org.keizar.server.utils.deleteAuthenticated
import org.keizar.server.utils.getAuthenticated
import org.keizar.server.utils.getUserId
import org.keizar.server.utils.postAuthenticated

fun Application.seedBankRouting(context: ServerContext) {
    val seedBank = context.seedBank

    routing {
        route("/seed-bank") {
            getAuthenticated {
                val userId = getUserId() ?: return@getAuthenticated
                call.respond(seedBank.getSeeds(userId))
            }

            postAuthenticated("/{seed}") {
                val userId = getUserId() ?: return@postAuthenticated
                val seed = call.parameters["seed"] ?: return@postAuthenticated
                if (seedBank.addSeed(userId, seed)) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Conflict)
                }
            }

            deleteAuthenticated("/{seed}") {
                val userId = getUserId() ?: return@deleteAuthenticated
                val seed = call.parameters["seed"] ?: return@deleteAuthenticated
                if (seedBank.removeSeed(userId, seed)) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}