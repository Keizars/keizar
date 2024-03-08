package org.keizar.android.ui.profile.component

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.profile.ProfileViewModel
import org.keizar.utils.communication.game.GameDataGet

class SavedGameViewModel(vm: ProfileViewModel, gameData: GameDataGet) : AbstractViewModel() {
    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl = _avatarUrl.asStateFlow()

    val isComputer = MutableStateFlow(false)

    private val _filePath = MutableStateFlow<String?>(null)
    val filePath = _filePath.asStateFlow()

    init {
        launchInBackground {
            if (gameData.opponentUsername == "Computer") {
                _avatarUrl.value = "https://keizar.s3.amazonaws.com/avatars/robot_icon.png"
                isComputer.value = true
            } else {
                _avatarUrl.value = vm.getAvatarUrl(gameData.opponentUsername)
                isComputer.value = false
            }
        }
    }


}