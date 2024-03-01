package org.keizar.android.ui.game.mp.room

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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

    //    
//    /**
//     * The current configuration of the board, to preview
//     */
//    @Stable
//    val boardProperties: StateFlow<BoardProperties>
    @Stable
    val accept: State<Boolean>

    suspend fun accept()

    @Stable
    val acceptButtonText: MutableState<String>

}

class PrivateRoomViewModelImpl(
    override val roomId: UInt
) : PrivateRoomViewModel, AbstractViewModel(), KoinComponent {
    private val facade: KeizarWebsocketClientFacade by inject()
    private val roomService: RoomService by inject()
    private val sessionManager: SessionManager by inject()

    override val playersReady: SharedFlow<Boolean> = flow {
        while (currentCoroutineContext().isActive) {
            emit(roomService.getRoom(roomId).playerInfo.all { it.isReady })
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
        }
    }.shareInBackground(started = SharingStarted.Eagerly)

    override val connectRoomError: MutableStateFlow<ConnectRoomError?> = MutableStateFlow(null)

    override val configuration: GameConfigurationViewModel = GameConfigurationViewModel(false)
    override val accept: MutableState<Boolean> = mutableStateOf(false)
    override val acceptButtonText: MutableState<String> = mutableStateOf("Ready!")

    private val showToast = mutableStateOf(false)

    init {
        backgroundScope.launch {
            configuration.boardProperties.collect {
                it.seed?.let { it1 -> setSeed(roomId, it1.toUInt()) }
                showToast.value = true
            }
        }
        backgroundScope.launch {
            val self = sessionManager.self
            client.first().players.filter { it.username == self.first().username}[0].state.collect {
                accept.value = it != PlayerSessionState.READY
            }
        }
    }

    private suspend fun setSeed(roomId: UInt, seed: UInt) {
        client.first().changeSeed(roomId, seed)
    }

    override suspend fun accept() {
        if (accept.value) {
            accept.value = false
            client.first().setReady()
        }
        if (accept.value) {
            acceptButtonText.value = "Ready!"
        } else {
            acceptButtonText.value = "Waiting for the opponent..."
        }
    }
//    val boardSeed = MutableStateFlow(0u)
//    override val boardProperties: StateFlow<BoardProperties> = boardSeed.map { BoardProperties(it) }


    override fun dispose() {
        super.dispose()
        configuration.dispose()
    }
}