package org.keizar.android.ui.game.mp.room

import androidx.compose.runtime.Stable
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.game.configuration.GameConfigurationViewModel

interface PrivateRoomViewModel {
    @Stable
    val configuration: GameConfigurationViewModel
//    
//    /**
//     * The current configuration of the board, to preview
//     */
//    @Stable
//    val boardProperties: StateFlow<BoardProperties>
}

class PrivateRoomViewModelImpl : PrivateRoomViewModel, AbstractViewModel() {
    override val configuration: GameConfigurationViewModel = GameConfigurationViewModel()
//    val boardSeed = MutableStateFlow(0u)
//    override val boardProperties: StateFlow<BoardProperties> = boardSeed.map { BoardProperties(it) }


    override fun dispose() {
        super.dispose()
        configuration.dispose()
    }
}