package org.keizar.android.ui.game.mp.room

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.game.configuration.GameConfigurationViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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
}

class PrivateRoomViewModelImpl : PrivateRoomViewModel, AbstractViewModel() {
    override val configuration: GameConfigurationViewModel = GameConfigurationViewModel()
    override val accept: MutableState<Boolean> = mutableStateOf(false)

    override fun clickAccept() {

    }
//    val boardSeed = MutableStateFlow(0u)
//    override val boardProperties: StateFlow<BoardProperties> = boardSeed.map { BoardProperties(it) }


    override fun dispose() {
        super.dispose()
        configuration.dispose()
    }
}