package org.keizar.android.ui.game.mp.room

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.keizar.android.client.RoomService
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.game.configuration.GameConfigurationViewModel
import org.keizar.android.ui.game.mp.MultiplayerLobbyScene
import org.keizar.client.KeizarClientFacade
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

/**
 * View model for the [MultiplayerLobbyScene]
 */
interface PrivateRoomViewModel {
    @Stable
    val roomId: UInt

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
    val accept: MutableState<Boolean>

    fun clickAccept()

    @Stable
    val acceptButtonText: MutableState<String>
}

class PrivateRoomViewModelImpl(
    override val roomId: UInt
) : PrivateRoomViewModel, AbstractViewModel(), KoinComponent {
    private val facade: KeizarClientFacade by inject()
    override val playersReady: SharedFlow<Boolean> = flow {
        while (currentCoroutineContext().isActive) {
            emit(facade.getRoom(roomId).playerInfo.all { it.isReady })
            delay(2.seconds)
        }
    }.flowOn(Dispatchers.IO)
        .distinctUntilChanged()
        .shareInBackground()


    override val configuration: GameConfigurationViewModel = GameConfigurationViewModel(false)
    override val accept: MutableState<Boolean> = mutableStateOf(false)
    override val acceptButtonText: MutableState<String> = mutableStateOf("Ready!")

    private val roomService: RoomService by inject()
    private val showToast = mutableStateOf(false)

    init {
        backgroundScope.launch {
            configuration.boardProperties.collect {
                it.seed?.let { it1 -> setSeed(roomId, it1.toUInt()) }
                accept.value = true
                showToast.value = true
            }
        }
    }

    private suspend fun setSeed(roomId: UInt, seed: UInt) {
        roomService.setSeed(roomId, seed)
    }

    override fun clickAccept() {
        if (accept.value) {
            accept.value = false
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