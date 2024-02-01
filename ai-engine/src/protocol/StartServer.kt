package org.keizar.aiengine.protocol

import org.keizar.aiengine.protocol.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.uri
import io.ktor.util.logging.KtorSimpleLogger

fun main() {
    embeddedServer(
        Netty,
        port = 49152,
        host = "0.0.0.0",
        module = Application::module,
        configure = {
            this.tcpKeepAlive = true
            this.connectionGroupSize = 40
            this.workerGroupSize = 40
            this.callGroupSize = 40
        }
    ).start(wait = true)
}


fun Application.module() {
    val context = setupServerContext()

    install(CallLogging)

    configureSerialization()
    configureRouting(context)
}

fun setupServerContext(): ServerContext {
    return ServerContext()
}
