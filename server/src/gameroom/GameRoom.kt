package org.keizar.server.gameroom

import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.keizar.game.BoardProperties
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.Exit
import org.keizar.utils.communication.message.PlayerAllocation
import org.keizar.utils.communication.message.Request
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.StateChange
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext

interface GameRoom {
    fun addPlayer(player: PlayerSession): Boolean
    fun close()

    val roomNumber: UInt
    val finished: StateFlow<Boolean>
    val properties: BoardProperties
    val numPlayer: Int
}

class GameRoomImpl(
    override val roomNumber: UInt,
    override val properties: BoardProperties,
    parentCoroutineContext: CoroutineContext,
    private val logger: Logger,
) : GameRoom {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    private val players: Channel<PlayerSession> = Channel(capacity = 2)

    private val _finished: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val finished: StateFlow<Boolean> = _finished.asStateFlow()
    override var numPlayer: Int = 0
        private set

    init {
        myCoroutineScope.launch { waitForPlayers() }
    }

    override fun addPlayer(player: PlayerSession): Boolean {
        return players.trySend(player).isSuccess
    }

    override fun close() {
        myCoroutineScope.cancel()
    }

    private suspend fun waitForPlayers() {
        val player1 = players.receive()
        player1.setState(PlayerSessionState.WAITING)
        ++numPlayer
        val player2 = players.receive()
        ++numPlayer
        players.cancel()
        startGame(player1, player2)
        updateFinished(player1, player2)
    }

    private suspend fun updateFinished(player1: PlayerSession, player2: PlayerSession) {
        player1.state.first { it == PlayerSessionState.TERMINATING }
        player2.state.first { it == PlayerSessionState.TERMINATING }
        _finished.value = true
    }

    private suspend fun startGame(player1: PlayerSession, player2: PlayerSession) {
        val playerAllocation = Player.entries.shuffled()
        notifyPlayerAllocation(player1, playerAllocation[0])
        notifyPlayerAllocation(player2, playerAllocation[1])

        player1.setState(PlayerSessionState.PLAYING)
        player2.setState(PlayerSessionState.PLAYING)

        myCoroutineScope.launch { forwardMessages(player1, player2) }
        myCoroutineScope.launch { forwardMessages(player2, player1) }
        myCoroutineScope.launch { notifyStateChange(player1) }
        myCoroutineScope.launch { notifyStateChange(player2) }
    }

    private suspend fun notifyPlayerAllocation(player: PlayerSession, allocation: Player) {
        player.session.sendRespond(PlayerAllocation(allocation))
    }

    private suspend fun forwardMessages(from: PlayerSession, to: PlayerSession) {
        while (true) {
            try {
                val message = from.session.receiveDeserialized<Request>()
                logger.info("Received request $message from $from")
                if (message == Exit) {
                    from.setState(PlayerSessionState.TERMINATING)
                    logger.info("$from exiting")
                    return
                }
                to.session.sendRequest(message)
                logger.info("Forwarded request $message to $to")
            } catch (e: WebsocketDeserializeException) {
                // ignore
            }
        }
    }

    private suspend fun notifyStateChange(player: PlayerSession) {
        player.state.collect { newState ->
            println("notifyStateChange: $newState")
            player.session.sendRespond(StateChange(newState))
        }
    }
}

suspend inline fun DefaultWebSocketServerSession.sendRespond(message: Respond) {
    sendSerialized(message)
}

suspend inline fun DefaultWebSocketServerSession.sendRequest(message: Request) {
    sendSerialized(message)
}