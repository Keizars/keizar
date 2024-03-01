package org.keizar.server.modules.gameroom

import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.keizar.game.BoardProperties
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.Exit
import org.keizar.utils.communication.message.RemoteSessionSetup
import org.keizar.utils.communication.message.Request
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.PlayerStateChange
import org.slf4j.Logger
import kotlin.coroutines.CoroutineContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.keizar.game.GameSession
import org.keizar.game.PlayerInfo
import org.keizar.game.snapshot.GameSnapshot
import org.keizar.utils.communication.message.ChangeBoard
import org.keizar.utils.communication.message.ConfirmNextRound
import org.keizar.utils.communication.message.Move
import org.keizar.utils.communication.message.RoomStateChange
import org.keizar.utils.communication.message.SetReady
import org.keizar.utils.communication.message.UserInfo
import kotlin.time.Duration.Companion.seconds

interface GameRoom : AutoCloseable {
    /**
     * Add a user into the room. Register them as one of the players, but not start the game.
     * If the player is already in the room, return true.
     */
    suspend fun join(user: UserInfo): Boolean

    /**
     * Connect to the user through a web socket session.
     * Calling connect() twice on the same [UserInfo] will override its registered [DefaultWebSocketServerSession].
     * The messages will be sent to the newest [DefaultWebSocketServerSession].
     */
    fun connect(user: UserInfo, session: DefaultWebSocketServerSession): PlayerSession?

    /**
     * Make a user ready to start the game. If the user is already ready, return true.
     * When two players are both ready, the game should start.
     * On the server side, it should start to send and forward messages through the
     * registered websockets.
     */
    fun ready(user: UserInfo): Boolean

    /**
     * Returns true if the user is already in the room.
     */
    fun containsPlayer(user: UserInfo): Boolean

    /**
     * Whether the user is the host (creator) of the room.
     */
    fun isHost(user: UserInfo): Boolean

    /**
     * List all players in the room.
     */
    fun listPlayers(): List<PlayerInfo>

    /**
     * Return whether each player is ready to start the game.
     */
    fun getIfPlayersReady(): Map<UserInfo, Boolean>

    val roomNumber: UInt
    val properties: BoardProperties

    /**
     * Indicate the state of the room.
     * Can be one of [ServerGameRoomState.Started], [ServerGameRoomState.AllConnected], [ServerGameRoomState.Playing], [ServerGameRoomState.Finished].
     */
    val state: Flow<ServerGameRoomState>

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
}

class GameRoomImpl(
    override val roomNumber: UInt,
    override val properties: BoardProperties,
    parentCoroutineContext: CoroutineContext,
    private val logger: Logger,
    private val heartbeatInterval: Long = 5.seconds.inWholeMilliseconds,
    private val dyingHeartbeatThreshold: Int = 60,
) : GameRoom {
    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        logger.error(e.message)
    }

    private val myCoroutineScope: CoroutineScope = CoroutineScope(
        parentCoroutineContext +
                SupervisorJob(parent = parentCoroutineContext[Job]) +
                exceptionHandler
    )

    override val state: MutableStateFlow<ServerGameRoomState> =
        MutableStateFlow(ServerGameRoomState.Started(properties))


    /**
     * Add players into the room and register player information.
     */

    override suspend fun join(user: UserInfo): Boolean {
        val curState = state.value as? ServerGameRoomState.Started ?: return false
        return curState.players {
            if (this.containsKey(user)) return@players true
            if (this.size >= Player.entries.size) return@players false

            val player = PlayerSession.create(
                user = user,
                playerAllocation = curState.allocatePlayer() ?: return@players false,
                isHost = this.isEmpty(),
            )
            this[user] = player
            startHeartbeat(player)
            startNotifyStateChange(player)
            startReceivingPreGameRequests(player)

            if (this.size == Player.entries.size) {
                state.value = curState.toAllConnected()
            }
            return@players true
        }
    }

    private fun startHeartbeat(player: PlayerSession) {
        myCoroutineScope.launch {
            while (true) {
                player.checkConnection()
                delay(heartbeatInterval)
            }
        }
    }

    private fun startNotifyStateChange(player: PlayerSession) {
        myCoroutineScope.launch {
            for (receiver in players.values) {
                player.state.collect { newState ->
                    logger.info("Notify player $receiver of player $player state change: $newState")
                    receiver.session.value?.sendRespond(
                        PlayerStateChange(player.user.username, newState)
                    )
                }
            }
        }
        myCoroutineScope.launch {
            this@GameRoomImpl.state.collect { newState ->
                logger.info("Notify player $player of room state change: $newState")
                player.session.value?.sendRespond(RoomStateChange(newState.toGameRoomState()))
            }
        }
    }

    private fun startReceivingPreGameRequests(player: PlayerSession) {
        val job = myCoroutineScope.launch {
            player.session.collectLatest {
                try {
                    val message = it?.receiveDeserialized<Request>() ?: return@collectLatest
                    logger.info("Received request $message from $player")
                    when (message) {
                        is ChangeBoard -> {
                            if (player.isHost) {
                                changeBoard(BoardProperties.fromJson(message.boardProperties))
                            }
                        }

                        is SetReady -> {
                            ready(player.user)
                        }

                        else -> {
                            // ignore
                        }
                    }
                } catch (e: WebsocketDeserializeException) {
                    // ignore
                }
            }
        }
        myCoroutineScope.launch {
            state.first { it is ServerGameRoomState.Playing || it is ServerGameRoomState.Finished }
            job.cancel()
        }
    }

    /**
     * Change the board properties of the game.
     * Doing so will remove all players from their ready state.
     */
    private suspend fun changeBoard(newProperties: BoardProperties): Boolean {
        val curState = state.value as? ServerGameRoomState.StateWithModifiableBoardPropertiesServer
            ?: return false
        curState.boardProperties = newProperties
        when (curState) {
            is ServerGameRoomState.Started -> {
                curState.players {
                    for (player in this.values) {
                        player.setState(PlayerSessionState.STARTED)
                        notifyRemoteSessionSetup(player)
                    }
                }
            }

            is ServerGameRoomState.AllConnected -> {
                for (player in curState.players.values) {
                    player.setState(PlayerSessionState.STARTED)
                    notifyRemoteSessionSetup(player)
                }
            }
        }
        return true
    }


    /**
     * Functions for querying player information.
     */

    private val players get() = state.value.players

    override fun containsPlayer(user: UserInfo): Boolean {
        return players.containsKey(user)
    }

    override fun isHost(user: UserInfo): Boolean {
        return players[user]?.isHost ?: false
    }

    override fun listPlayers(): List<PlayerInfo> {
        return players.map { (user, playerSession) ->
            PlayerInfo(
                user = user,
                isHost = playerSession.isHost,
                isReady = playerSession.state.value == PlayerSessionState.READY,
            )
        }
    }

    override fun getIfPlayersReady(): Map<UserInfo, Boolean> {
        return players.mapValues { (_, player) -> player.state.value == PlayerSessionState.READY }
    }


    /**
     * Connect players ([UserInfo]s) to websocket sessions ([DefaultWebSocketServerSession]s).
     */

    override fun connect(user: UserInfo, session: DefaultWebSocketServerSession): PlayerSession? {
        val playerSession = players[user] ?: return null
        playerSession.connect(session)
        myCoroutineScope.launch {
            notifyRemoteSessionSetup(playerSession)
            session.sendRespond(RoomStateChange(state.value.toGameRoomState()))
            for (player in players.values) {
                session.sendRespond(PlayerStateChange(player.user.username, player.state.value))
            }
        }
        logger.info("Session of user $user changed to $session")
        return playerSession
    }

    private suspend fun notifyRemoteSessionSetup(
        player: PlayerSession,
    ) {
        val allocation = player.playerAllocation
        val gameSnapshot = when (val curState = state.value) {
            is ServerGameRoomState.StateWithModifiableBoardPropertiesServer ->
                GameSession.create(curState.boardProperties).getSnapshot()

            is ServerGameRoomState.Playing -> curState.serverGame.getSnapshot()
            is ServerGameRoomState.Finished -> return
        }
        logger.info("Notify user $player of allocation $allocation and gameSnapshot")
        player.session.value?.sendRespond(
            RemoteSessionSetup(
                allocation,
                Json.encodeToJsonElement(gameSnapshot)
            )
        )
    }


    /**
     * Make players ready to start the game.
     */

    override fun ready(user: UserInfo): Boolean {
        val curState = state.value as? ServerGameRoomState.AllConnected ?: return false
        val playerSession = players[user] ?: return false
        playerSession.setState(PlayerSessionState.READY)
        checkAllPlayersReady(curState)
        return true
    }

    private fun checkAllPlayersReady(curState: ServerGameRoomState.AllConnected) {
        if (players.values.all { it.state.value == PlayerSessionState.READY }) {
            state.value = curState.toPlaying()
        }
    }


    /**
     * Start the game. Contains functions for running a game.
     */

    init {
        startWaitingForPlayers()
    }

    private fun startWaitingForPlayers() {
        myCoroutineScope.launch {
            state.first { it is ServerGameRoomState.Playing }
            startGame()
            checkAllFinished()
        }
    }

    private fun checkAllFinished() {
        myCoroutineScope.launch {
            val curState = state.value as ServerGameRoomState.Playing
            var dyingHeartbeats = 0
            while (true) {
                delay(heartbeatInterval)
                if (dyingHeartbeats >= dyingHeartbeatThreshold) {
                    state.value = curState.toFinished()
                } else if (
                    players.values.all { it.state.value == PlayerSessionState.TERMINATING }
                ) {
                    state.value = curState.toFinished()
                } else if (
                    players.values.all {
                        it.state.value == PlayerSessionState.DISCONNECTED ||
                                it.state.value == PlayerSessionState.TERMINATING
                    }
                ) {
                    ++dyingHeartbeats
                } else {
                    dyingHeartbeats = 0
                }
            }
        }
    }

    override fun close() {
        myCoroutineScope.cancel()
    }

    private suspend fun startGame() {
        val curState = state.value as ServerGameRoomState.Playing

        players.values.map { player ->
            player.setState(PlayerSessionState.PLAYING)
        }

        val (player1, player2) = players.values.toList()

        myCoroutineScope.launch {
            player2.session.collectLatest {
                forwardMessages(player2, player1)
            }
        }

        myCoroutineScope.launch {
            player1.session.collectLatest {
                forwardMessages(player1, player2)
            }
        }
    }

    private suspend fun forwardMessages(
        from: PlayerSession,
        to: PlayerSession,
    ) {
        val player = from.playerAllocation
        val curState = state.value as ServerGameRoomState.Playing
        while (true) {
            try {
                val message = from.session.value?.receiveDeserialized<Request>() ?: return
                logger.info("Received request $message from $from")
                myCoroutineScope.launch {
                    when (message) {
                        ConfirmNextRound -> {
                            if (!curState.serverGame.confirmNextRound(player)) {
                                logger.info("Player $from sends invalid confirmNextRound")
                            }
                            to.session.value?.sendRequest(message)
                        }

                        Exit -> {
                            from.setState(PlayerSessionState.TERMINATING)
                            logger.info("$from exiting")
                        }

                        is Move -> {
                            if (!curState.serverGame.currentRound.first()
                                    .move(message.from, message.to)
                            ) {
                                logger.info("Player $from sends invalid move $message")
                            }
                            to.session.value?.sendRequest(message)
                        }

                        else -> {
                            // ignore
                        }
                    }
                }
                logger.info("Forwarded request $message to $to")
            } catch (e: WebsocketDeserializeException) {
                // ignore
            }
        }
    }
}

suspend inline fun DefaultWebSocketServerSession.sendRespond(message: Respond) {
    sendSerialized(message)
}

suspend inline fun DefaultWebSocketServerSession.sendRequest(message: Request) {
    sendSerialized(message)
}