@file:JvmName("ApplicationKt")

package org.keizar.server

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import org.keizar.server.plugins.configureDatabases
import org.keizar.server.plugins.configureRouting
import org.keizar.server.plugins.configureSecurity
import org.keizar.server.plugins.configureSerialization
import org.keizar.server.plugins.configureSockets
import org.keizar.server.training.module

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
        port = 80,
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
    configureSecurity()
    configureSerialization()
    configureDatabases()
    configureSockets()
    configureRouting()
}
