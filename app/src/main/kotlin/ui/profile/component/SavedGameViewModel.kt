package org.keizar.android.ui.profile.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.keizar.android.ui.profile.ProfileViewModel
import org.keizar.utils.communication.game.GameDataGet

class SavedGameViewModel(vm: ProfileViewModel, gameData: GameDataGet): ViewModel() {
    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl = _avatarUrl.asStateFlow()

    val isComputer = MutableStateFlow(false)
    
    private val _filePath = MutableStateFlow<String?>(null)
    val filePath = _filePath.asStateFlow()
    init {
        viewModelScope.launch {
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