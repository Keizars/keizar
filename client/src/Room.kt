package org.keizar.client

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.keizar.client.internal.AbstractWebsocketSessionHandler
import org.keizar.client.internal.GameSessionWsHandlerImpl
import org.keizar.game.BoardProperties
import org.keizar.game.RoomInfo
import org.keizar.game.snapshot.GameSnapshot
import org.keizar.utils.communication.GameRoomState
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.account.User
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.ChangeBoard
import org.keizar.utils.communication.message.PlayerStateChange
import org.keizar.utils.communication.message.RemoteSessionSetup
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.RoomStateChange
import org.keizar.utils.communication.message.SetReady
import kotlin.coroutines.CoroutineContext


/**
 * A game room for the 2-player games that contains information of the room and the players in the room.
 *
 * ## [Room] is stateful
 * It maintains a connection with the server and synchronizes the state of the room and the players.
 */
interface Room : AutoCloseable {
    /**
     * Room number of the game room
     */
    val roomNumber: UInt

    /**
     * Information of the user who is using this client, including the username,
     * the nickname, and the avatarUrl of the user.
     */
    val self: User

    /**
     * List of player information of each user in the room.
     *
     * Information contains the username of the player, whether the player is the host,
     * and the state of player: one of [PlayerSessionState.STARTED], [PlayerSessionState.READY],
     * [PlayerSessionState.PLAYING], [PlayerSessionState.DISCONNECTED],
     * and [PlayerSessionState.TERMINATING].
     */
    val players: List<ClientPlayer>

    /**
     * Player information of the the self user.
     */
    val selfPlayer: ClientPlayer

    /**
     * Player information of the the self user.
     *
     * The flow emits not null iff all players are connected to the room,
     * i.e. [state] is [GameRoomState.ALL_CONNECTED] or further.
     */
    val opponentPlayer: StateFlow<ClientPlayer?>

    /**
     * State of the game room: one of [GameRoomState.STARTED], [GameRoomState.ALL_CONNECTED],
     * [GameRoomState.PLAYING],and [GameRoomState.FINISHED].
     *
     * State is set to [GameRoomState.STARTED] when the room is created.
     * When both player connects to the same room on the server,
     * the state changes to [GameRoomState.ALL_CONNECTED].
     * When all players are ready and the game starts, the state changes to [GameRoomState.PLAYING].
     * When the game is finished, the state changes to [GameRoomState.FINISHED].
     */
    val state: SharedFlow<GameRoomState>

    /**
     * The current board properties of the game.
     *
     * When the room is in state [GameRoomState.STARTED] or [GameRoomState.ALL_CONNECTED],
     * the room may receive RemoteSessionSetup messages from the server indicating that the
     * host has changed the seed of the room. In this case, the boardProperties flow will emit
     * the new board properties.
     */
    val boardProperties: Flow<BoardProperties>

    /**
     * Send a request to server attempting to change the seed of the room.
     *
     * When called in state [GameRoomState.STARTED] or [GameRoomState.ALL_CONNECTED],
     * room seed will be changed to the new seed.
     *
     * Otherwise, this function immediately returns.
     * Only the host can change the seed. If a non-host player calls this method,
     * it will have no effect.
     */
    suspend fun changeSeed(newSeed: UInt)

    /**
     * Send a request to server attempting to set the player's state to ready.
     *
     * Call to this method will succeed when the room is in state [GameRoomState.STARTED] or
     * [GameRoomState.ALL_CONNECTED] and the player's state is [PlayerSessionState.STARTED].
     * Otherwise, it will have no effect.
     */
    suspend fun setReady()

    /**
     * Create and return a RemoteGameSession for the room specified by its room number.
     * Should be called only when state of the room is [GameRoomState.PLAYING].
     * If called on other states, it will wait until the state changes to [GameRoomState.PLAYING].
     * Multiple calls to this method will return the same RemoteGameSession instance.
     */
    suspend fun getGameSession(): RemoteGameSession

    companion object {
        internal fun create(
            self: User,
            roomInfo: RoomInfo,
            websocketSession: DefaultClientWebSocketSession,
            parentCoroutineContext: CoroutineContext
        ): Room {
            return RoomImpl(
                self = self,
                roomNumber = roomInfo.roomNumber,
                players = roomInfo.playerInfo.map {
                    ClientPlayer(
                        it.user.username,
                        it.isHost,
                        it.isReady,
                    )
                }.toMutableList(),
                session = websocketSession,
                parentCoroutineContext = parentCoroutineContext,
            )
        }
    }
}

private class RoomImpl(
    override val self: User,
    override val roomNumber: UInt,
    override val players: MutableList<ClientPlayer>,
    /**
     * Websocket session used to communicate with the room on server and synchronize the states.
     */
    private val session: DefaultClientWebSocketSession,
    parentCoroutineContext: CoroutineContext
) : Room {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    override val selfPlayer: ClientPlayer get() = players.first { it.username == self.username }
    override val opponentPlayer: MutableStateFlow<ClientPlayer?> =
        MutableStateFlow(players.firstOrNull { it.username != self.username })

    private val _state = MutableSharedFlow<GameRoomState>(replay = 1)
    override val state: SharedFlow<GameRoomState> = _state

    /**
     *  These two variables are set by RemoteSessionSetup message from the server.
     *  After the room connects to the server, the server will send a RemoteSessionSetup message,
     *  and the room will emit values to these two flows upon receiving the message.
     */
    private val playerAllocation: MutableSharedFlow<Player> = MutableSharedFlow(replay = 1)
    private val gameSnapshot: MutableSharedFlow<GameSnapshot> = MutableSharedFlow(replay = 1)

    override val boardProperties: Flow<BoardProperties> = gameSnapshot.map { it.properties }

    /**
     * This websocket session handler is active until the room state changes to
     * [GameRoomState.PLAYING]. It is used for receiving pre-game updates from the server,
     * including changes of room state, player state, and game setup (player allocation and
     * [BoardProperties]).
     *
     * When room state changes to [GameRoomState.PLAYING], this handler is closed and the control
     * of the websocket session is transferred to a [GameSessionWsHandlerImpl] handled by
     * a [RemoteGameSession], both created by [getGameSession].
     */
    private val websocketSessionHandler =
        object : AbstractWebsocketSessionHandler(
            session = session,
            parentCoroutineContext = myCoroutineScope.coroutineContext,
            cancelWebsocketOnExit = false
        ) {
            override suspend fun processResponse(respond: Respond) {
                println("Pregame websocket handler processing: $respond")
                when (respond) {
                    is PlayerStateChange -> {
                        val player = players.firstOrNull { it.username == respond.username }
                        if (player == null) {
                            val newPlayer = ClientPlayer(
                                username = respond.username,
                                isHost = false,
                                initialState = respond.newState,
                            )
                            players.add(newPlayer)
                            opponentPlayer.value = newPlayer
                        } else {
                            player.setState(respond.newState)
                        }
                    }

                    is RoomStateChange -> _state.emit(respond.newState)

                    is RemoteSessionSetup -> {
                        playerAllocation.emit(respond.playerAllocation)
                        gameSnapshot.emit(
                            Json.decodeFromJsonElement(
                                GameSnapshot.serializer(),
                                respond.gameSnapshot
                            )
                        )
                    }

                    else -> {
                        // ignore
                    }
                }
            }
        }

    init {
        myCoroutineScope.launch { start() }
    }

    /**
     * Connect to the websocket session and start receiving updates
     * Automatically called when the client is created.
     */
    private suspend fun start() {
        websocketSessionHandler.start()
        println("Pregame websocket handler started")
    }

    override fun close() {
        websocketSessionHandler.close()
        println("Pregame websocket handler closed")
        myCoroutineScope.cancel()
    }

    override suspend fun changeSeed(newSeed: UInt) {
        if (websocketSessionHandler.isClosed) return
        val properties =
            BoardProperties.toJson(BoardProperties.getStandardProperties(newSeed.toInt()))
        return websocketSessionHandler.sendRequest(ChangeBoard(properties))
    }

    override suspend fun setReady() {
        if (websocketSessionHandler.isClosed) return
        return websocketSessionHandler.sendRequest(SetReady)
    }

    private var game: RemoteGameSession? = null
    private val createGameMutex = Mutex()

    /**
     * Create and retrieve a RemoteGameSession for the room using the singleton pattern.
     * If the room is not in state [GameRoomState.PLAYING], this method will wait until then to
     * make sure that the [playerAllocation] and [gameSnapshot] are set to the correct values.
     *
     * Also closes [websocketSessionHandler] and transfers the control of the websocket session to
     * a [GameSessionWsHandlerImpl] handled by the returned [RemoteGameSession].
     */
    override suspend fun getGameSession(): RemoteGameSession {
        createGameMutex.withLock {
            if (game == null) {
                state.first { it == GameRoomState.PLAYING }

                /**
                 * Retrieve the non-null values of playerAllocation and gameSnapshot before
                 * closing the websocketSessionHandler: if the player reconnects to the room
                 * halfway through the game, the playerAllocation and gameSnapshot should
                 * still be able to be set up properly.
                 */
                val selfPlayer = playerAllocation.first()
                val gameSnapshot = gameSnapshot.first()
                websocketSessionHandler.close()
                println("Pregame websocket handler closed")

                val wsHandler = GameSessionWsHandlerImpl(
                    parentCoroutineContext = myCoroutineScope.coroutineContext,
                    session = session,
                    selfPlayer = selfPlayer,
                    onPlayerStateChange = { respond ->
                        players.firstOrNull { it.username == respond.username }
                            ?.setState(respond.newState)
                    },
                    onRoomStateChange = { respond ->
                        _state.emit(respond.newState)
                    },
                )
                game = RemoteGameSession.createAndConnect(gameSnapshot, wsHandler)
            }
        }
        return game!!
    }
}

class ClientPlayer(
    /**
     * Username of the player
     */
    val username: String,
    /**
     * Whether the player is the host of the room, i.e. the player who created the room
     */
    val isHost: Boolean,
    initialState: PlayerSessionState,
) {
    private val _state: MutableStateFlow<PlayerSessionState> = MutableStateFlow(initialState)

    constructor(username: String, isHost: Boolean, initialIsReady: Boolean) : this(
        username,
        isHost,
        if (initialIsReady) PlayerSessionState.READY else PlayerSessionState.STARTED,
    )

    /**
     * State of the player: one of [PlayerSessionState.STARTED], [PlayerSessionState.READY],
     * [PlayerSessionState.PLAYING], [PlayerSessionState.DISCONNECTED],
     * and [PlayerSessionState.TERMINATING].
     */
    val state: StateFlow<PlayerSessionState> = _state

    internal fun setState(newState: PlayerSessionState) {
        _state.value = newState
    }
}