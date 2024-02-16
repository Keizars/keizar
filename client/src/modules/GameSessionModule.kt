package org.keizar.client.modules

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.serialization.WebsocketDeserializeException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.keizar.client.exception.NetworkFailureException
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.utils.communication.CommunicationModule
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.ConfirmNextRound
import org.keizar.utils.communication.message.Move
import org.keizar.utils.communication.message.PlayerAllocation
import org.keizar.utils.communication.message.Request
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.StateChange
import org.keizar.utils.communication.message.UserInfo
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.random.nextUInt

internal interface GameSessionModule : AutoCloseable {
    fun getCurrentRole(): StateFlow<Role>
    fun getPlayerState(): Flow<PlayerSessionState>
    suspend fun getPlayer(): Player
    fun bind(session: GameSession)
    fun sendConfirmNextRound()
    fun sendMove(from: BoardPos, to: BoardPos)
    suspend fun connect(userInfo: UserInfo)
}

internal class GameSessionModuleImpl(
    private val roomNumber: UInt,
    parentCoroutineContext: CoroutineContext,
    private val client: KeizarHttpClient,
) : GameSessionModule {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    private val playerState: MutableStateFlow<PlayerSessionState> =
        MutableStateFlow(PlayerSessionState.STARTED)

    private lateinit var gameSession: GameSession
    private val player: CompletableDeferred<Player> = CompletableDeferred()
    private val currentRole: MutableStateFlow<Role> = MutableStateFlow(Role.WHITE)

    private val outflowChannel: Channel<Request> = Channel()

    override suspend fun connect(userInfo: UserInfo) {
        try {
            serverConnection(userInfo)
            startUpdateCurRole()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw NetworkFailureException(cause = e)
        }
    }

    override fun close() {
        myCoroutineScope.cancel()
    }

    private fun startUpdateCurRole() {
        myCoroutineScope.launch {
            val player = getPlayer()
            gameSession.currentRoundNo.collect { curRoundNo ->
                currentRole.value = gameSession.getRole(player, curRoundNo)
            }
        }
    }

    private suspend fun serverConnection(userInfo: UserInfo) {
        val session = client.getRoomWebsocketSession(roomNumber)

        session.sendSerialized(userInfo)

        myCoroutineScope.launch {
            session.messageInflow()
        }
        myCoroutineScope.launch {
            session.messageOutflow()
        }
    }

    private suspend fun DefaultClientWebSocketSession.messageOutflow() {
        while (true) {
            try {
                val request = outflowChannel.receive()
                println("Client sending: $request")
                sendSerialized(request)
            } catch (e: CancellationException) {
                // ignore
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.messageInflow() {
        while (true) {
            try {
                val respond = receiveDeserialized<Respond>()
                println("Client received: $respond")
                when (respond) {
                    is StateChange -> playerState.value = respond.newState
                    is PlayerAllocation -> player.complete(respond.who)
                    ConfirmNextRound -> gameSession.confirmNextRound(player.await().opponent())
                    is Move -> {
                        gameSession.currentRound.first().move(respond.from, respond.to)
                    }
                }
            } catch (e: WebsocketDeserializeException) {
                // ignore
            }
        }
    }

    override fun getCurrentRole(): StateFlow<Role> {
        return currentRole
    }

    override fun getPlayerState(): Flow<PlayerSessionState> {
        return playerState
    }

    override suspend fun getPlayer(): Player {
        return player.await()
    }

    override fun bind(session: GameSession) {
        gameSession = session
    }

    override fun sendConfirmNextRound() {
        myCoroutineScope.launch {
            outflowChannel.send(ConfirmNextRound)
        }
    }

    override fun sendMove(from: BoardPos, to: BoardPos) {
        myCoroutineScope.launch {
            outflowChannel.send(Move(from, to))
        }
    }
}

suspend inline fun DefaultClientWebSocketSession.sendRespond(message: Respond) {
    sendSerialized(message)
}

suspend inline fun DefaultClientWebSocketSession.sendRequest(message: Request) {
    sendSerialized(message)
}