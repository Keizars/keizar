package org.keizar.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.utils.communication.PlayerSessionState
import kotlin.coroutines.CoroutineContext
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.WebsocketDeserializeException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.ConfirmNextRound
import org.keizar.utils.communication.message.Exit
import org.keizar.utils.communication.message.Move
import org.keizar.utils.communication.message.PlayerAllocation
import org.keizar.utils.communication.message.Request
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.StateChange
import org.keizar.utils.communication.message.UserInfo

interface GameRoomClient {
    fun getCurrentRole(): StateFlow<Role>
    fun getPlayerState(): Flow<PlayerSessionState>
    fun getPlayer(): Player
    fun bind(session: GameSession)
    fun sendConfirmNextRound()
    fun sendMove(from: BoardPos, to: BoardPos)
    fun close()
}

class GameRoomClientImpl(
    private val roomNumber: UInt,
    parentCoroutineContext: CoroutineContext,
) : GameRoomClient {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    private val client = HttpClient {
        install(WebSockets)
    }

    private val playerState: MutableStateFlow<PlayerSessionState> =
        MutableStateFlow(PlayerSessionState.STARTED)

    private lateinit var gameSession: GameSession
    private lateinit var player: Player
    private val currentRole: MutableStateFlow<Role> = MutableStateFlow(Role.WHITE)

    private val outflowChannel: Channel<Request> = Channel()

    init {
        myCoroutineScope.launch {
            serverConnection()
            startUpdateCurRole()
        }
    }

    override fun close() {
        client.close()
        myCoroutineScope.cancel()
    }

    private suspend fun startUpdateCurRole() {
        val player = getPlayer()
        gameSession.currentRoundNo.collect { curRoundNo ->
            currentRole.value = gameSession.getRole(player, curRoundNo)
        }
    }

    private suspend fun serverConnection() {
        client.webSocket(
            method = HttpMethod.Get,
            host = "127.0.0.1",
            port = 80,
            path = "/room/$roomNumber"
        ) {
            myCoroutineScope.launch { messageInflow() }
            myCoroutineScope.launch { messageOutflow() }
        }
    }

    private suspend fun DefaultClientWebSocketSession.messageOutflow() {
        while (true) {
            try {
                val request = outflowChannel.receive()
                sendSerialized(request)
            } catch (e: CancellationException) {
                // ignore
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.messageInflow() {
        while (true) {
            try {
                when (val respond = receiveDeserialized<Respond>()) {
                    is StateChange -> playerState.value = respond.newState
                    is PlayerAllocation -> player = respond.who
                    ConfirmNextRound -> gameSession.confirmNextRound(player.opponent())
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

    // Note: initialization of Player's value is only guaranteed after playerState becomes PLAYING.
    // TODO: improve this
    override fun getPlayer(): Player {
        return player
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