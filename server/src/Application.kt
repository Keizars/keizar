@file:JvmName("ApplicationKt")

package org.keizar.server

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.callloging.CallLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.keizar.server.plugins.configureDatabases
import org.keizar.server.plugins.configureMultiplayerRouting
import org.keizar.server.plugins.configureSecurity
import org.keizar.server.plugins.configureSerialization
import org.keizar.server.plugins.configureSockets
import org.keizar.server.training.plugins.configureTrainingRouting

fun main() {
    getServer().start(wait = true)
}

suspend fun runServer(block: suspend () -> Unit) {
    val server = getServer()
    server.start(wait = false)
    block()
    server.stop()
}

private fun getServer(): NettyApplicationEngine {
    return embeddedServer(
        Netty,
        port = System.getenv("PORT")?.toInt() ?: 4392,
        host = "0.0.0.0",
        module = Application::module,
        configure = {
            this.tcpKeepAlive = true
            this.connectionGroupSize = 40
            this.workerGroupSize = 40
            this.callGroupSize = 40
        }
    )
}

fun Application.module() {
    val serverCoroutineScope = CoroutineScope(SupervisorJob())
    val context = setupServerContext(serverCoroutineScope, log)

    install(CallLogging) {
        mdc("requestId") {
            it.request.queryParameters["requestId"]
        }
        level = org.slf4j.event.Level.INFO
    }

    configureSecurity(context)
    configureSerialization()
    configureDatabases()
    configureSockets()
    configureMultiplayerRouting(context)
    configureTrainingRouting(context)
}
