package org.keizar.android.ui.game.mp.room

import androidx.compose.runtime.Stable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.android.ui.game.configuration.GameConfigurationViewModel
import org.keizar.android.ui.game.mp.MultiplayerLobbyScene
import org.keizar.client.ClientPlayer
import org.keizar.client.Room
import org.keizar.client.exception.RoomFullException
import org.keizar.client.services.RoomService
import org.keizar.client.services.UserService
import org.keizar.utils.communication.GameRoomState
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.account.User
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

enum class ConnectRoomError {
    ROOM_FULL,
    NETWORK_ERROR,
}

/**
 * View model for the [MultiplayerLobbyScene]
 */
interface PrivateRoomViewModel : HasBackgroundScope {
    /**
     * The room id of the room.
     */
    @Stable
    val roomId: UInt

    /**
     * The error that occurred when trying to connect to the room.
     * If ROOM_FULL, it shows that the there are already 2 players in the room.
     * If NETWORK_ERROR, the error that occurred is network error..
     * If null, no error occurred.
     */
    @Stable
    val connectRoomError: StateFlow<ConnectRoomError?>

    /**
     * Whether both players are ready to start the game.
     */
    @Stable
    val playersReady: SharedFlow<Boolean>

    /**
     * The game configuration of the room.
     */
    @Stable
    val configuration: GameConfigurationViewModel

    /**
     * Whether the self player is ready to start the game.
     */
    @Stable
    val selfReady: Flow<Boolean>

    /**
     * Notifies the server that the self player is ready to start the game.
     */
    suspend fun ready()

    /**
     * Whether the self player is the host of the room.
     */
    @Stable
    val selfIsHost: Flow<Boolean>

    /**
     * The name of the opponent player.
     */
    @Stable
    val opponentUser: Flow<User>

    /**
     * Whether the opponent player is ready to start the game.
     */
    @Stable
    val opponentReady: Flow<Boolean>

    /**
     * The opponent player in the room.
     */
    @Stable
    val opponentPlayer: Flow<ClientPlayer?>

}

class PrivateRoomViewModelImpl(
    override val roomId: UInt
) : PrivateRoomViewModel, AbstractViewModel(), KoinComponent {
    private val roomService: RoomService by inject()
    private val userService: UserService by inject()

    private val client: SharedFlow<Room> = flow {
        while (currentCoroutineContext().isActive) {
            val client = try {
                roomService.connect(roomId, parentCoroutineContext = backgroundScope.coroutineContext)
            } catch (e: RoomFullException) {
                e.printStackTrace()
                connectRoomError.value = ConnectRoomError.ROOM_FULL
                return@flow
            } catch (e: Throwable) {
                e.printStackTrace()
                connectRoomError.value = ConnectRoomError.NETWORK_ERROR
                delay(5.seconds)
                continue
            }
            emit(client)
            return@flow
        }
    }.shareInBackground(started = SharingStarted.Eagerly)

    private val selfPlayer = client.map { it.selfPlayer }

    override val opponentPlayer = client.flatMapLatest { it.opponentPlayer }

    override val connectRoomError: MutableStateFlow<ConnectRoomError?> = MutableStateFlow(null)

    override val configuration: GameConfigurationViewModel = GameConfigurationViewModel()
    override val selfReady: Flow<Boolean> = selfPlayer.flatMapLatest { it.state }.map { it == PlayerSessionState.READY }
    override val opponentReady: Flow<Boolean> =
        opponentPlayer.flatMapLatest { it?.state ?: emptyFlow() }.map { it == PlayerSessionState.READY }

    init {
        backgroundScope.launch {
            configuration.boardProperties.collect {
                it.seed?.let { it1 -> setSeed(it1.toUInt()) }
            }
        }
        backgroundScope.launch {

        }
    }

    override val selfIsHost: Flow<Boolean> = selfPlayer.mapLatest { it.isHost }
    override val opponentUser: Flow<User> = opponentPlayer
        .mapLatest { it?.username }
        .filterNotNull()
        .map {
            userService.getUser(it)
        }
        .shareInBackground(started = SharingStarted.Eagerly)

    override val playersReady: SharedFlow<Boolean> =
        client.flatMapLatest { it.state }.map { it == GameRoomState.PLAYING }.flowOn(Dispatchers.IO)
            .distinctUntilChanged()
            .shareInBackground()

    private suspend fun setSeed(seed: UInt) {
        client.first().changeSeed(seed)
    }

    override suspend fun ready() {
        if (!selfReady.first()) {
            client.first().setReady()
        }
    }

    override fun dispose() {
        super.dispose()
        configuration.dispose()
        client.replayCache.firstOrNull()?.let { room ->
            runCatching {
                room.close()
            }
        }
    }
}