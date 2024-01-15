package org.keizar.android.ui.game

import kotlinx.coroutines.flow.MutableStateFlow
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.game.BoardPos

class GameBoardViewModel : AbstractViewModel() {
    val pickedPos = MutableStateFlow<BoardPos?>(null)
}