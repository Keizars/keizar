package org.keizar.server.gameroom

import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.keizar.game.BoardProperties
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.Exit
import org.keizar.utils.communication.message.RemoteSessionSetup
import org.keizar.utils.communication.message.Request
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.StateChange
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.keizar.game.GameSession
import org.keizar.game.snapshot.GameSnapshot
import org.keizar.utils.communication.message.ConfirmNextRound
import org.keizar.utils.communication.message.Move
import org.keizar.utils.communication.message.UserInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicInteger

interface GameRoom : AutoCloseable {
    /**
     * Connect to the user through a web socket session. When two players both connect,
     * the game should start automatically. (On the server side, it should start to send
     * and forward messages through the websockets.)
     * Calling connect() twice on the same [UserInfo] will override its registered [PlayerSession].
     * The messages will be sent to the newest [PlayerSession].
     */
    suspend fun connect(user: UserInfo, player: PlayerSession): Boolean

    /**
     * Add a user into the room. Register them as one of the players, but not start the game.
     * If the player is already in the room, return true.
     */
    fun addPlayer(user: UserInfo): Boolean

    val roomNumber: UInt
    val properties: BoardProperties

    /**
     * Whether the game has finished.
     */
    val finished: Flow<Boolean>

    /**
     * Number of players that are [addPlayer()]-ed to the room.
     */
    val playerCount: Int

    /**
     * Whether all players added are ready to start the game.
     */
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


    /**
     * Add players into the room and register player information.
     */

    private val playerInfo: MutableList<UserInfo> = mutableListOf()
    private val _playerCount: AtomicInteger = AtomicInteger(0)
    override val playerCount: Int get() = _playerCount.get()
    override var playersReady = false
    private val playersMutex = Mutex()

    private val playerAllocation: ConcurrentMap<UserInfo, Player> = ConcurrentHashMap()

    override fun addPlayer(user: UserInfo): Boolean {
        if (user in playerInfo) return true
        val playerIndex = _playerCount.getAndIncrement()
        if (playerIndex < Player.entries.size) {
            myCoroutineScope.launch {
                playersMutex.withLock {
                    playerInfo.add(playerIndex, user)
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

    override fun containsPlayer(user: UserInfo): Boolean {
        return user in playerInfo
    }


    /**
     * Determine which player is the FirstBlackPlayer and which is the FirstWhitePlayer.
     */

    private val allPlayers = Player.entries.shuffled()
    private val allPlayersIndex = AtomicInteger(0)
    private fun allocatePlayer(user: UserInfo) {
        if (playerAllocation.containsKey(user)) return
        val index = allPlayersIndex.getAndIncrement()
        if (index >= allPlayers.size) return
        playerAllocation[user] = allPlayers[index]
    }


    /**
     * Connect players ([UserInfo]s) to websocket sessions ([PlayerSession]s).
     */

    private val playerSessions: MutableMap<UserInfo, MutableStateFlow<PlayerSession>> =
        mutableMapOf()
    private val playersConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun connect(user: UserInfo, player: PlayerSession): Boolean {
        if (user in playerInfo) {
            notifyRemoteSessionSetup(
                player = player,
                allocation = playerAllocation[player.user]!!,
                gameSnapshot = serverGame.getSnapshot()
            )

            if (!playerSessions.containsKey(user)) {
                playerSessions[user] = MutableStateFlow(player)
            } else {
                val oldSession = playerSessions[user]!!.value
                oldSession.cancel("Websocket session $oldSession expired")
                playerSessions[user]!!.value = player
            }
            logger.info("Session of user $user changed to $player")

            if (playerSessions.keys.containsAll(playerInfo)) {
                playersConnected.value = true
            }
            return true
        }
        return false
    }


    /**
     * Start the game. Contains functions for running a game.
     */

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

    private val serverGame: GameSession = GameSession.create(properties)

    private fun startGame() {
        playerSessions.values.map {
            myCoroutineScope.launch {
                it.collectLatest { player ->
                    player.setState(PlayerSessionState.PLAYING)
                }
            }

            myCoroutineScope.launch {
                notifyStateChange(it)
            }
        }

        val player1 = playerSessions[playerInfo[0]]!!
        val player2 = playerSessions[playerInfo[1]]!!

        myCoroutineScope.launch {
            player2.collectLatest {
                forwardMessages(player2, player1)
            }
        }

        myCoroutineScope.launch {
            player1.collectLatest {
                forwardMessages(player1, player2)
            }
        }
    }

    private suspend fun notifyRemoteSessionSetup(
        player: PlayerSession,
        allocation: Player,
        gameSnapshot: GameSnapshot
    ) {
        logger.info("Notify user $player of player allocation: $allocation and gameSnapshot")
        player.session.sendRespond(RemoteSessionSetup(
            allocation,
            Json.encodeToJsonElement(gameSnapshot)
        ))
    }

    private suspend fun forwardMessages(
        from: StateFlow<PlayerSession>,
        to: StateFlow<PlayerSession>
    ) {
        val player = playerAllocation[from.value.user]!!
        while (true) {
            try {
                val message = from.value.session.receiveDeserialized<Request>()
                logger.info("Received request $message from ${from.value}")
                myCoroutineScope.launch {
                    when (message) {
                        ConfirmNextRound -> {
                            if (!serverGame.confirmNextRound(player)) {
                                logger.info("Player ${from.value} sends invalid confirmNextRound")
                            }
                            to.value.session.sendRequest(message)
                        }

                        Exit -> {
                            from.value.setState(PlayerSessionState.TERMINATING)
                            logger.info("$from exiting")
                        }

                        is Move -> {
                            if (!serverGame.currentRound.first().move(message.from, message.to)) {
                                logger.info("Player ${from.value} sends invalid move $message")
                            }
                            to.value.session.sendRequest(message)
                        }

                        is UserInfo -> {}
                    }
                }
                logger.info("Forwarded request $message to ${to.value}")
            } catch (e: WebsocketDeserializeException) {
                // ignore
            }
        }
    }

    private suspend fun notifyStateChange(player: StateFlow<PlayerSession>) {
        player.value.state.collect { newState ->
            logger.info("Notify player ${player.value} of state change: $newState")
            player.value.session.sendRespond(StateChange(newState))
        }
    }
}

suspend inline fun DefaultWebSocketServerSession.sendRespond(message: Respond) {
    sendSerialized(message)
}

suspend inline fun DefaultWebSocketServerSession.sendRequest(message: Request) {
    sendSerialized(message)
}