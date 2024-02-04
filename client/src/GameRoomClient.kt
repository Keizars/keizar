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
import io.ktor.websocket.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.PlayerAllocation
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.StateChange

interface GameRoomClient {
    fun getCurrentRole(): StateFlow<Role>
    fun getPlayerState(): Flow<PlayerSessionState>
    fun getPlayer(): Player
    fun bind(session: GameSession)
    fun sendConfirmNextRound()
    fun sendMove(from: BoardPos, to: BoardPos)
}

class GameRoomClientImpl(
    private val roomNumber: UInt,
    parentCoroutineContext: CoroutineContext,
) : GameRoomClient {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    private val playerState: MutableStateFlow<PlayerSessionState> =
        MutableStateFlow(PlayerSessionState.STARTED)

    private lateinit var gameSession: GameSession
    private lateinit var playerAllocation: Player
    private val currentRole: MutableStateFlow<Role> = MutableStateFlow(Role.WHITE)

    init {
        myCoroutineScope.launch {
            serverConnection()
            startUpdateCurRole()
        }
    }

    private suspend fun startUpdateCurRole() {
        val player = getPlayer()
        gameSession.currentRoundNo.collect { curRoundNo ->
            currentRole.value = gameSession.getRole(player, curRoundNo)
        }
    }

    private suspend fun serverConnection() {
        val client = HttpClient {
            install(WebSockets)
        }
        client.webSocket(
            method = HttpMethod.Get,
            host = "127.0.0.1",
            port = 80,
            path = "/room/$roomNumber"
        ) {
            val messageInflowRoutine = myCoroutineScope.launch { messageInflow() }
            val messageOutflowRoutine = myCoroutineScope.launch { messageOutflow() }

            messageOutflowRoutine.join()
            messageInflowRoutine.cancelAndJoin()
        }
        client.close()
    }

    private suspend fun DefaultClientWebSocketSession.messageOutflow() {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                println(message.readText())
            }
        } catch (e: Exception) {
            println("Error while receiving: " + e.localizedMessage)
        }
    }

    private suspend fun DefaultClientWebSocketSession.messageInflow() {
        while (true) {
            try {
                when (val message = receiveDeserialized<Respond>()) {
                    is StateChange -> playerState.value = message.newState
                    is PlayerAllocation -> playerAllocation = message.who
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
        return playerAllocation
    }

    override fun bind(session: GameSession) {
        gameSession = session
    }

    override fun sendConfirmNextRound() {
        TODO("Not yet implemented")
    }

    override fun sendMove(from: BoardPos, to: BoardPos) {
        TODO("Not yet implemented")
    }
}