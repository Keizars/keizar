package org.keizar.server.training

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import org.keizar.server.training.plugins.configureSerialization
import org.keizar.server.training.plugins.configureTrainingRouting

fun main() {
    embeddedServer(
        Netty,
        port = 49152,
        host = "127.0.0.1",
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
    configureTrainingRouting(context)
}

fun setupServerContext(): ServerContext {
    return ServerContext()
}
