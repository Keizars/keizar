package org.keizar.android.ui.game.mp.room

import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.game.configuration.GameConfigurationViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.keizar.android.client.RoomService
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject

interface PrivateRoomViewModel {
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
    private val roomId: UInt
) : PrivateRoomViewModel, AbstractViewModel(), KoinComponent {
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
    private suspend fun setSeed(roomId:UInt, seed: UInt) {
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