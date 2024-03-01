package org.keizar.client

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.ChangeBoard
import org.keizar.utils.communication.message.PlayerStateChange
import org.keizar.utils.communication.message.RemoteSessionSetup
import org.keizar.utils.communication.message.Respond
import org.keizar.utils.communication.message.RoomStateChange
import org.keizar.utils.communication.message.SetReady
import kotlin.coroutines.CoroutineContext


interface GameRoomClient : AutoCloseable {
    val roomNumber: UInt
    val players: List<ClientPlayer>
    val state: StateFlow<GameRoomState>

    /**
     * Changes the seed of the room
     */
    suspend fun changeSeed(roomNumber: UInt, seed: UInt)

    /**
     * Set the player's state to ready
     */
    suspend fun setReady(roomNumber: UInt, seed: UInt)

    /**
     * Create and return a RemoteGameSession for the room specified by its room number.
     * Should be called only when state of the room is [GameRoomState.PLAYING].
     * If called on other states, it will wait until the state changes to [GameRoomState.PLAYING].
     */
    suspend fun getGameSession(): RemoteGameSession

    /**
     * Connect to the websocket session and start receiving updates
     */
    suspend fun start()

    companion object {
        internal fun create(
            roomInfo: RoomInfo,
            websocketSession: DefaultClientWebSocketSession,
            parentCoroutineContext: CoroutineContext
        ): GameRoomClient {
            return GameRoomClientImpl(
                roomNumber = roomInfo.roomNumber,
                players = roomInfo.playerInfo.map {
                    ClientPlayer(
                        it.user.username,
                        it.isHost,
                        it.isReady,
                    )
                },
                session = websocketSession,
                parentCoroutineContext = parentCoroutineContext,
            )
        }
    }
}

class GameRoomClientImpl internal constructor(
    override val roomNumber: UInt,
    override val players: List<ClientPlayer>,
    private val session: DefaultClientWebSocketSession,
    parentCoroutineContext: CoroutineContext
) : GameRoomClient {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    private val _state: MutableStateFlow<GameRoomState> = MutableStateFlow(GameRoomState.STARTED)
    override val state: StateFlow<GameRoomState> = _state

    private val playerAllocation: MutableStateFlow<Player?> = MutableStateFlow(null)
    private val gameSnapshot: MutableStateFlow<GameSnapshot?> = MutableStateFlow(null)

    private val websocketSessionHandler =
        object : AbstractWebsocketSessionHandler(session, myCoroutineScope.coroutineContext) {
            override suspend fun processResponse(respond: Respond) {
                when (respond) {
                    is PlayerStateChange -> {
                        players.firstOrNull { it.username == respond.username }
                            ?.setState(respond.newState)
                    }

                    is RemoteSessionSetup -> {
                        playerAllocation.value = respond.playerAllocation
                        gameSnapshot.value = Json.decodeFromJsonElement(
                            GameSnapshot.serializer(),
                            respond.gameSnapshot
                        )
                    }

                    is RoomStateChange -> _state.value = respond.newState
                    else -> {
                        // ignore
                    }
                }
            }
        }

    override suspend fun start() {
        websocketSessionHandler.start()
    }

    override fun close() {
        websocketSessionHandler.close()
        myCoroutineScope.cancel()
    }

    override suspend fun changeSeed(roomNumber: UInt, seed: UInt) {
        val properties = BoardProperties.toJson(BoardProperties.getStandardProperties(seed.toInt()))
        return websocketSessionHandler.sendRequest(ChangeBoard(properties))
    }

    override suspend fun setReady(roomNumber: UInt, seed: UInt) {
        return websocketSessionHandler.sendRequest(SetReady)
    }

    private var game: RemoteGameSession? = null
    private val createGameMutex = Mutex()
    override suspend fun getGameSession(): RemoteGameSession {
        createGameMutex.withLock {
            if (game == null) {
                state.first { it == GameRoomState.PLAYING }
                val wsHandler = GameSessionWsHandlerImpl(
                    parentCoroutineContext = myCoroutineScope.coroutineContext,
                    session = session,
                    selfPlayer = playerAllocation.first { it != null }!!,
                    gameSnapshot = gameSnapshot.first { it != null }!!,
                )
                game = RemoteGameSession.createAndConnect(wsHandler)
            }
        }
        return game!!
    }
}

class ClientPlayer(
    val username: String,
    val isHost: Boolean,
    initialIsReady: Boolean,
) {
    private val _state: MutableStateFlow<PlayerSessionState> = MutableStateFlow(
        if (initialIsReady) PlayerSessionState.READY else PlayerSessionState.STARTED
    )
    val state: StateFlow<PlayerSessionState> = _state

    internal fun setState(newState: PlayerSessionState) {
        _state.value = newState
    }
}