package org.keizar.server.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import org.keizar.utils.communication.CommunicationModule

val ServerJson = Json {
    ignoreUnknownKeys = true
    serializersModule = CommunicationModule
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(ServerJson)
    }
}
