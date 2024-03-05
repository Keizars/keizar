package org.keizar.client

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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


interface GameRoomClient : AutoCloseable {
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
     * Send a request to server attempting to change the seed of the room.
     *
     * When called in state [GameRoomState.STARTED] or [GameRoomState.ALL_CONNECTED],
     * room seed will be changed to the new seed.
     * Otherwise, it will have no effect.
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
        ): GameRoomClient {
            return GameRoomClientImpl(
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

class GameRoomClientImpl internal constructor(
    override val self: User,
    override val roomNumber: UInt,
    override val players: MutableList<ClientPlayer>,
    /**
     * Websocket session used to communicate with the room on server and synchronize the states.
     */
    private val session: DefaultClientWebSocketSession,
    parentCoroutineContext: CoroutineContext
) : GameRoomClient {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    override val selfPlayer: ClientPlayer get() = players.first { it.username == self.username }
    override val opponentPlayer: MutableStateFlow<ClientPlayer?> =
        MutableStateFlow(players.firstOrNull { it.username != self.username })

    private val _state = MutableSharedFlow<GameRoomState>(replay = 1)
    override val state: SharedFlow<GameRoomState> = _state

    /**
     *  These two variables are set by RemoteSessionSetup message from the server.
     *  The values are guaranteed to be non-null when the room state changes to
     *  [GameRoomState.ALL_CONNECTED].
     */
    private val playerAllocation: MutableStateFlow<Player?> = MutableStateFlow(null)
    private val gameSnapshot: MutableStateFlow<GameSnapshot?> = MutableStateFlow(null)

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
                        playerAllocation.value = respond.playerAllocation
                        gameSnapshot.value = Json.decodeFromJsonElement(
                            GameSnapshot.serializer(),
                            respond.gameSnapshot
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
    }

    override fun close() {
        websocketSessionHandler.close()
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
                val selfPlayer = playerAllocation.filterNotNull().first()
                val gameSnapshot = gameSnapshot.filterNotNull().first()
                websocketSessionHandler.close()

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