package org.keizar.server.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.keizar.server.ServerContext
import org.keizar.server.utils.getUserId
import org.keizar.server.utils.routeAuthenticated

fun Application.seedBankRouting(context: ServerContext) {
    val seedBank = context.seedBank

    routeAuthenticated {
        route("/seed-bank") {
            get {
                val userId = getUserId() ?: return@get
                call.respond(seedBank.getSeeds(userId))
            }

            post("/{seed}") {
                val userId = getUserId() ?: return@post
                val seed = call.parameters["seed"] ?: return@post
                call.respond(seedBank.addSeed(userId, seed))
            }

            delete("/{seed}") {
                val userId = getUserId() ?: return@delete
                val seed = call.parameters["seed"] ?: return@delete
                call.respond(seedBank.removeSeed(userId, seed))
            }
        }
    }
}