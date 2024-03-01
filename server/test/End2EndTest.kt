package org.keizar.server

import io.ktor.client.request.post
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Test
import org.keizar.server.plugins.configureDatabases
import org.keizar.server.plugins.configureSecurity
import org.keizar.server.plugins.configureSerialization
import org.keizar.server.plugins.configureSockets
import org.keizar.server.routing.usersRouting
import org.solvo.server.modules.authenticationRouting

class End2EndTest {
    private fun Application.basicSetup(): ServerContext {
        val serverCoroutineScope = CoroutineScope(SupervisorJob())
        val context = setupServerContext(
            serverCoroutineScope, log, EnvironmentVariables(
                testing = "true",
                mongoDbConnectionString = "",
            )
        )

        configureSecurity(context)
        configureSerialization()
        configureDatabases()
        configureSockets()
        return context
    }

    @Test
    fun testUsersModule() = testApplication {
        application {
            val context = basicSetup()
            authenticationRouting(context)
            usersRouting(context)
        }

        val response = client.post("/upload") {
            // TODO
        }
    }
}