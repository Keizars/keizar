package org.keizar.aiengine.protocol

import org.keizar.aiengine.protocol.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.*

fun main() {
    embeddedServer(CIO, port = 49152, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val context = setupServerContext()

    configureSerialization()
    configureRouting(context)
}

fun setupServerContext(): ServerContext {
    return ServerContext()
}
