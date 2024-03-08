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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.keizar.client.RemoteGameSession
import org.keizar.client.Room
import org.keizar.game.BoardProperties
import org.keizar.game.Role
import org.keizar.utils.communication.GameRoomState
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.account.AuthRequest
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.GameResult
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class End2EndTest {
    private val env = EnvironmentVariables(
        port = 4392,
        testing = true,
        mongoDbConnectionString = ""
    )
    private val random = Random

    private val server = getServer(env)

    @Test
    fun test() = runTest(timeout = 30.seconds) {
        val coroutineScope = CoroutineScope(Job())
        val startGuest = MutableStateFlow(false)
        val roomNo = 5462
        val hostJob = coroutineScope.launch {
            hostClientCoroutine(coroutineScope, roomNo, startGuest)
        }
        startGuest.first { it }
        val guestJob = coroutineScope.launch {
            guestClientCoroutine(coroutineScope, roomNo)
        }

        hostJob.join()
        guestJob.join()
        try {
            coroutineScope.cancel()
        } catch (e: CancellationException) {
            // ignore
        }
    }

    private suspend fun hostClientCoroutine(
        coroutineScope: CoroutineScope,
        roomNo: Int,
        startGuest: MutableStateFlow<Boolean>,
    ) {
        val endpoint = "http://localhost:${env.port}"
        val client = HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val username = "user-${random.nextInt() % 1000}"
        val token = client.post("$endpoint/users/register") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(username = username, password = username))
        }.body<AuthResponse>().token
        assertNotNull(token)

        val responseCreate = client.post("$endpoint/room/$roomNo/create") {
            contentType(ContentType.Application.Json)
            setBody(BoardProperties.getStandardProperties())
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, responseCreate.status)
        client.close()


        val gameRoom = connectToRoom(
            roomNo = roomNo.toUInt(),
            coroutineContext = coroutineScope.coroutineContext,
            token = token,
            endpoint = endpoint
        )

        assertEquals(roomNo.toUInt(), gameRoom.roomNumber)
        assertEquals(GameRoomState.STARTED, gameRoom.state.first())
        assertEquals(username, gameRoom.self.username)
        assertEquals(PlayerSessionState.STARTED, gameRoom.selfPlayer.state.value)
        assertTrue(gameRoom.selfPlayer.isHost)

        startGuest.value = true

        val hostProposedSeed = 100
        gameRoom.changeSeed(hostProposedSeed.toUInt())
        gameRoom.setReady()

        gameRoom.state.first { it == GameRoomState.PLAYING }
        val game = gameRoom.getGameSession()
        assertEquals(hostProposedSeed, game.properties.seed)

        val result = startGame(game, coroutineScope)
        assertEquals(GameResult.Draw, result)
    }

    private suspend fun guestClientCoroutine(
        coroutineScope: CoroutineScope,
        roomNo: Int,
    ) {
        val endpoint = "http://localhost:${env.port}"
        val client = HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val username = "user-${random.nextInt() % 1000}"
        val token = client.post("$endpoint/users/register") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(username = username, password = username))
        }.body<AuthResponse>().token
        assertNotNull(token)

        val responseJoin = client.post("$endpoint/room/$roomNo/join") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, responseJoin.status)
        client.close()

        val gameRoom = connectToRoom(
            roomNo = roomNo.toUInt(),
            coroutineContext = coroutineScope.coroutineContext,
            token = token,
            endpoint = endpoint
        )

        assertEquals(roomNo.toUInt(), gameRoom.roomNumber)
        assertEquals(username, gameRoom.self.username)
        assertEquals(PlayerSessionState.STARTED, gameRoom.selfPlayer.state.value)
        assertFalse(gameRoom.selfPlayer.isHost)

        val guestProposedSeed = 200
        gameRoom.changeSeed(guestProposedSeed.toUInt())
        gameRoom.setReady()

        gameRoom.state.first { it == GameRoomState.PLAYING }
        val game = gameRoom.getGameSession()
        assertNotEquals(guestProposedSeed, game.properties.seed)

        val result = startGame(game, coroutineScope)
        assertEquals(GameResult.Draw, result)
    }

    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    private suspend fun connectToRoom(
        roomNo: UInt,
        coroutineContext: CoroutineContext,
        token: String,
        endpoint: String,
    ): Room {
        val client: org.keizar.client.internal.KeizarHttpClient =
            org.keizar.client.internal.KeizarHttpClientImpl(endpoint)
        val self = client.getSelf(token)
        val websocketSession = client.getRoomWebsocketSession(roomNo, token)
        val roomInfo = client.getRoom(roomNo, token)
        return Room.create(
            self = self,
            roomInfo = roomInfo,
            websocketSession = websocketSession,
            parentCoroutineContext = coroutineContext
        )
    }

    private suspend fun startGame(
        game: RemoteGameSession,
        coroutineScope: CoroutineScope
    ): GameResult {
        val selfPlayer = game.player
        val job1 = coroutineScope.launch {
            game.currentRound.collectLatest { round ->
                delay(1000)
                val roundNo = game.currentRoundNo.first()
                val selfRole = game.getRole(selfPlayer, roundNo)
                var moveCnt = 0
                round.curRole.filter { it == selfRole }.collect {
                    val (from, to) = if (selfRole == Role.WHITE) {
                        whiteMoves[moveCnt]
                    } else {
                        blackMoves[moveCnt]
                    }
                    round.move(BoardPos(from), BoardPos(to))
                    ++moveCnt
                }
            }
        }
        val job2 = coroutineScope.launch {
            game.currentRound.collectLatest { round ->
                round.winner.first { it != null }
                game.confirmNextRound(selfPlayer)
            }
        }
        val finalWinner = game.finalWinner.filterNotNull().first()
        try {
            job1.cancel()
            job2.cancel()
        } catch (e: CancellationException) {
            // ignore
        }
        return finalWinner
    }

    private val whiteMoves = listOf(
        "d2" to "d3",
        "d3" to "d4",
        "d4" to "d5",
        "e2" to "e3",
        "e3" to "e4",
        "e4" to "e5",
        "f2" to "f3",
    )

    private val blackMoves = listOf(
        "h7" to "h6",
        "h6" to "h5",
        "h5" to "h4",
        "h4" to "h3",
        "g7" to "g6",
        "g6" to "g5",
        "g5" to "g4",
    )

    @BeforeEach
    fun setUp() {
        server.start(wait = false)
    }

    @AfterEach
    fun tearDown() {
        server.stop(1000, 1000)
    }
}