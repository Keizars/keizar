package org.keizar.android.ui.game.mp.room

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.keizar.android.client.RoomService
import org.keizar.android.client.SessionManager
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.android.ui.game.configuration.GameConfigurationViewModel
import org.keizar.android.ui.game.mp.MultiplayerLobbyScene
import org.keizar.client.GameRoomClient
import org.keizar.client.KeizarWebsocketClientFacade
import org.keizar.client.exception.RoomFullException
import org.keizar.utils.communication.PlayerSessionState
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

    @Stable
    val connectRoomError: StateFlow<ConnectRoomError?>

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

    @Stable
    val selfIsHost: Flow<Boolean>

    @Stable
    val opponentName: Flow<String>

}

class PrivateRoomViewModelImpl(
    override val roomId: UInt
) : PrivateRoomViewModel, AbstractViewModel(), KoinComponent {
    private val facade: KeizarWebsocketClientFacade by inject()
    private val roomService: RoomService by inject()
    private val sessionManager: SessionManager by inject()

    override val playersReady: SharedFlow<Boolean> = flow {
        while (currentCoroutineContext().isActive) {
            val playerInfo = roomService.getRoom(roomId.toString()).playerInfo
            emit(playerInfo.size == 2 && playerInfo.all { it.isReady })
            delay(2.seconds)
        }
    }.flowOn(Dispatchers.IO)
        .distinctUntilChanged()
        .shareInBackground()

    private val client: Flow<GameRoomClient> = flow {
        while (currentCoroutineContext().isActive) {
            val client = try {
                facade.connect(roomId, parentCoroutineContext = backgroundScope.coroutineContext)
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

    private val opponentPlayer = client.map { it.opponentPlayer }

    override val connectRoomError: MutableStateFlow<ConnectRoomError?> = MutableStateFlow(null)

    override val configuration: GameConfigurationViewModel = GameConfigurationViewModel()
    override val selfReady: Flow<Boolean> = selfPlayer.flatMapLatest { it.state }.map { it == PlayerSessionState.READY }

    private val showToast = mutableStateOf(false)

    init {
        backgroundScope.launch {
            configuration.boardProperties.collect {
                it.seed?.let { it1 -> setSeed(it1.toUInt()) }
                showToast.value = true
            }
        }
    }

    override val selfIsHost: Flow<Boolean> = selfPlayer.mapLatest { it.isHost }

    override val opponentName: Flow<String> = opponentPlayer.mapLatest { it?.username ?: "" }

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
    }
}