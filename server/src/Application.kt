@file:JvmName("ApplicationKt")

package org.keizar.server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.keizar.server.plugins.configureDatabases
import org.keizar.server.plugins.configureMultiplayerRouting
import org.keizar.server.plugins.configureSecurity
import org.keizar.server.plugins.configureSerialization
import org.keizar.server.plugins.configureSockets

fun main() {
    getServer().start(wait = true)
}

internal fun getServer(
    env: EnvironmentVariables = EnvironmentVariables(),
): NettyApplicationEngine {
    return embeddedServer(
        Netty,
        port = env.port ?: 4392,
        host = "0.0.0.0",
        module = { module(env) },
        configure = {
            this.tcpKeepAlive = true
            this.connectionGroupSize = 40
            this.workerGroupSize = 40
            this.callGroupSize = 40
        }
    )
}

fun Application.module(env: EnvironmentVariables) {
    val serverCoroutineScope = CoroutineScope(SupervisorJob())
    val context = setupServerContext(serverCoroutineScope, log, env)

    install(CallLogging) {
        mdc("requestId") {
            it.request.queryParameters["requestId"]
        }
        level = org.slf4j.event.Level.INFO
    }
    install(StatusPages) {
        exception<Throwable> { call, throwable ->
            throwable.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }

    configureSecurity(context)
    configureSerialization()
    configureDatabases()
    configureSockets()
    configureMultiplayerRouting(context)
}
