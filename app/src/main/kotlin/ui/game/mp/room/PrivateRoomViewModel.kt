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
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.yield
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.warn
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.ErrorMessage
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.android.ui.game.configuration.GameConfigurationViewModel
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.mp.MultiplayerLobbyScene
import org.keizar.client.ClientPlayer
import org.keizar.client.Room
import org.keizar.client.exceptions.RoomFullException
import org.keizar.client.services.RoomService
import org.keizar.client.services.UserService
import org.keizar.game.Role
import org.keizar.utils.communication.GameRoomState
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.account.User
import org.keizar.utils.communication.game.Difficulty
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

    @Stable
    val errorDialog: StateFlow<ErrorMessage?>

    @Stable
    val errorToast: StateFlow<ErrorMessage?>

    /**
     * Whether both players are ready to start the game.
     */
    @Stable
    val playersReady: SharedFlow<Boolean>

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

    /**
     * The board change from other client.
     */
    suspend fun boardChangeFromOtherClientUpdate()

}

class PrivateRoomViewModelImpl(
    override val roomId: UInt
) : PrivateRoomViewModel, AbstractViewModel(), KoinComponent {
    private val roomService: RoomService by inject()
    private val userService: UserService by inject()

    private val client: SharedFlow<Room> = flow {
        while (currentCoroutineContext().isActive) {
            try {
                logger.info { "PrivateRoom roomService.connect: Connecting" }
                val room = roomService.connect(
                    roomId,
                    parentCoroutineContext = backgroundScope.coroutineContext
                )
                errorDialog.value = null
                emit(room)
                logger.info { "roomService.connect: successfully" }

                // Wait until session closes
                select { room.onComplete {} }
                if (!currentCoroutineContext().isActive) {
                    logger.warn { "Room connection closed, current coroutine is not active, returning" }
                    return@flow
                }
                logger.warn { "Room connection closed, current coroutine is active, attempting to reconnect after 2 seconds" }
                yield() // check cancellation
                errorDialog.value = ErrorMessage.networkErrorRecovering()
                delay(2.seconds)
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
        }
    }.shareInBackground(started = SharingStarted.Eagerly)

    private val selfPlayer = client.map { it.selfPlayer }

    override val opponentPlayer = client.flatMapLatest { it.opponentPlayer }
    private val boardChangeFromOtherClient: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override suspend fun boardChangeFromOtherClientUpdate() {
        boardChangeFromOtherClient.value = false
    }

    override val connectRoomError: MutableStateFlow<ConnectRoomError?> = MutableStateFlow(null)

    override val errorDialog: MutableStateFlow<ErrorMessage?> = MutableStateFlow(null)
    override val errorToast: MutableStateFlow<ErrorMessage?> = MutableStateFlow(null)

    override val configuration: GameConfigurationViewModel = GameConfigurationViewModel()
    override val selfReady: Flow<Boolean> =
        selfPlayer.flatMapLatest { it.state }.map { it >= PlayerSessionState.READY }
    override val opponentReady: Flow<Boolean> =
        opponentPlayer.flatMapLatest { it?.state ?: emptyFlow() }
            .map { it >= PlayerSessionState.READY }

    init {
        backgroundScope.launch {
            configuration.boardProperties.collect {
                it.seed?.let { it1 -> setSeed(it1.toUInt()) }
            }
        }
        backgroundScope.launch {
            client.flatMapLatest { it.boardProperties }.collect { boardProperties ->
                boardProperties.seed?.let { it1 -> changeBoardProperties(it1) }
            }
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
        if (boardChangeFromOtherClient.value || !selfIsHost.first()) {
            return
        } else {
            try {
                client.first().changeSeed(seed)
            } catch (e: Exception) {
                errorToast.value = ErrorMessage.networkError()
            }
        }
//        try {
//            client.first().changeSeed(seed)
//        } catch (e: Exception) {
//            errorToast.value = ErrorMessage.networkError()
//        }
    }

    private fun changeBoardProperties(layoutSeed: Int) {
        val newGameStartConfiguration =
            GameStartConfiguration(
                layoutSeed = layoutSeed,
                playAs = Role.WHITE,
                difficulty = Difficulty.MEDIUM
            )
        configuration.setNewConfiguration(newGameStartConfiguration)
        boardChangeFromOtherClient.value = true
    }

    override suspend fun ready() {
        try {
            if (!selfReady.first()) {
                client.first().setReady()
            }
        } catch (e: Exception) {
            errorToast.value = ErrorMessage.networkError()
            throw e
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