package org.keizar.server.training

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.put
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.keizar.server.training.plugins.AIBoardData

class ServerTest {
    @Test
    fun `test board`() = testApplication {
        application { module() }
        val response = client.put("/board/0")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `test moves white`() = testApplication {
        application { module() }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val response = client.post("/moves/white") {
            contentType(ContentType.Application.Json)
            setBody(AIBoardData(listOf(listOf(0, 0, 0), listOf(0, 0, 0))))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }
}