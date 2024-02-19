package org.keizar.server.gameroom

import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.utils.communication.message.UserInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicInteger

interface GameRoom : AutoCloseable {
    suspend fun connect(user: UserInfo, player: PlayerSession): Boolean
    fun addPlayer(user: UserInfo): Boolean

    val roomNumber: UInt
    val finished: Flow<Boolean>
    val properties: BoardProperties
    val playerCount: Int
    val playersReady: Boolean

    companion object {
        fun create(
            roomNumber: UInt,
            properties: BoardProperties,
            parentCoroutineContext: CoroutineContext,
            logger: Logger,
        ): GameRoom {
            return GameRoomImpl(roomNumber, properties, parentCoroutineContext, logger)
        }
    }

    fun containsPlayer(user: UserInfo): Boolean
}

class GameRoomImpl(
    override val roomNumber: UInt,
    override val properties: BoardProperties,
    parentCoroutineContext: CoroutineContext,
    private val logger: Logger,
) : GameRoom {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    private val playerInfos: MutableList<UserInfo> = mutableListOf()
    private val _playerCount: AtomicInteger = AtomicInteger(0)
    override val playerCount: Int get() = _playerCount.get()
    override var playersReady = false
    private val playersMutex = Mutex()

    private val playerAllocation: ConcurrentMap<UserInfo, Player> = ConcurrentHashMap()

    override fun addPlayer(user: UserInfo): Boolean {
        val playerIndex = _playerCount.getAndIncrement()
        if (playerIndex < Player.entries.size) {
            myCoroutineScope.launch {
                playersMutex.withLock {
                    playerInfos.add(playerIndex, user)
                }
            }
            if (playerIndex == Player.entries.size - 1) {
                playersReady = true
            }
            allocatePlayer(user)
            return true
        }
        return false
    }

    private val allPlayers = Player.entries.shuffled()
    private val allPlayersIndex = AtomicInteger(0)
    private fun allocatePlayer(user: UserInfo) {
        if (playerAllocation.containsKey(user)) return
        val index = allPlayersIndex.getAndIncrement()
        if (index >= allPlayers.size) return
        playerAllocation[user] = allPlayers[index]
    }

    override fun containsPlayer(user: UserInfo): Boolean {
        return user in playerInfos
    }

    private val playerSessions: MutableMap<UserInfo, MutableSharedFlow<PlayerSession>> =
        mutableMapOf()
    private val playersConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun connect(user: UserInfo, player: PlayerSession): Boolean {
        if (user in playerInfos) {
            player.setState(PlayerSessionState.WAITING)
            notifyPlayerAllocation(player, playerAllocation[player.user]!!)
            if (!playerSessions.containsKey(user)) {
                playerSessions[user] = MutableSharedFlow()
            }
            playerSessions[user]!!.emit(player)
            if (playerSessions.keys.containsAll(playerInfos)) {
                playersConnected.value = true
            }
            return true
        }
        return false
    }

    init {
        startWaitingForPlayers()
    }

    private fun startWaitingForPlayers() {
        myCoroutineScope.launch {
            playersConnected.first { it }
            startGame()
            updateFinished()
        }
    }

    private var _finished: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override var finished: StateFlow<Boolean> = _finished.asStateFlow()

    private suspend fun updateFinished() {
        combine(playerSessions.values.map { session ->
            session.flatMapLatest { it.state }
        }) { states ->
            states.all { it == PlayerSessionState.TERMINATING }
        }.first { it }
        _finished.value = true
    }

    override fun close() {
        myCoroutineScope.cancel()
    }

    private fun startGame() {
        playerSessions.values.map {
            myCoroutineScope.launch {
                it.collectLatest { player ->
                    myCoroutineScope.launch { notifyStateChange(player) }
                }
            }
        }

        myCoroutineScope.launch {
            combine(playerSessions.values) { (player1, player2) ->
                myCoroutineScope.launch { forwardMessages(player1, player2) }
                myCoroutineScope.launch { forwardMessages(player2, player1) }
            }.collectLatest { }
        }
    }

    private suspend fun notifyPlayerAllocation(player: PlayerSession, allocation: Player) {
        logger.info("Notify user $player of player allocation: $allocation")
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
            logger.info("Notify player $player of state change: $newState")
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