package org.keizar.server

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.keizar.client.KeizarWebsocketClientFacade
import org.keizar.game.BoardProperties
import org.keizar.utils.communication.GameRoomState
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.account.AuthRequest
import org.keizar.utils.communication.account.AuthResponse
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class End2EndTest {
    private val env = EnvironmentVariables(
        port = 4392,
        testing = true,
        mongoDbConnectionString = ""
    )

    private val server = getServer(env)

    @Test
    fun test() = runTest {
        val coroutineScope = CoroutineScope(Job())
        val job = coroutineScope.launch { hostClientCoroutine(coroutineScope) }
        job.join()
        try {
            coroutineScope.cancel()
        } catch (e: CancellationException) {
            // ignore
        }
    }

    private suspend fun hostClientCoroutine(coroutineScope: CoroutineScope) {
        val endpoint = "http://localhost:${env.port}"
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }.use { client ->
            val username = "user-${Random.nextInt() % 1000}"
            val token = client.post("$endpoint/users/register") {
                contentType(ContentType.Application.Json)
                setBody(AuthRequest(username = username, password = username))
            }.body<AuthResponse>().token
            assertNotNull(token)

            val roomNo = 5462
            val responseCreate = client.post("$endpoint/room/$roomNo/create") {
                contentType(ContentType.Application.Json)
                setBody(BoardProperties.getStandardProperties())
                bearerAuth(token)
            }
            assertEquals(HttpStatusCode.OK, responseCreate.status)
            val responseJoin = client.post("$endpoint/room/$roomNo/join") {
                bearerAuth(token)
            }
            assertEquals(HttpStatusCode.OK, responseJoin.status)

            val clientFacade = KeizarWebsocketClientFacade(endpoint, MutableStateFlow(token))
            val gameRoom = clientFacade.connect(roomNo.toUInt(), coroutineScope.coroutineContext)

            assertEquals(roomNo.toUInt(), gameRoom.roomNumber)
            assertEquals(GameRoomState.STARTED, gameRoom.state.value)
            assertEquals(username, gameRoom.self.username)
            assertEquals(PlayerSessionState.STARTED, gameRoom.selfPlayer.state.value)
            assertTrue(gameRoom.selfPlayer.isHost)
        }
    }

    @BeforeEach
    fun setUp() {
        server.start(wait = false)
    }

    @AfterEach
    fun tearDown() {
        server.stop(0, 0)
        coroutineScope.cancel()
//        runBlocking { coroutineScope.coroutineContext[Job]!!.cancelAndJoin() }
    }
}