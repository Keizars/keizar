package org.keizar.server

import io.ktor.client.request.post
import io.ktor.server.application.log
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Test
import org.keizar.server.routing.accountRouting
import org.solvo.server.modules.authenticationRouting

class End2EndTest {
//    @Test
//    fun testAccountModule() = testApplication {
//        application {
//            val serverCoroutineScope = CoroutineScope(SupervisorJob())
//            val context = setupServerContext(serverCoroutineScope, log)
//
//            authenticationRouting(context)
//            accountRouting(context)
//        }
//
//        val response = client.post("/upload") {
//            TODO()
//        }
//    }
}