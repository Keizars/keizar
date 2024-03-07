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
    
    private val _filePath = MutableStateFlow<String?>(null)
    val filePath = _filePath.asStateFlow()
    init {
        viewModelScope.launch {
            if (gameData.opponentUsername == "Computer") {
                _filePath.value = "app/src/main/res/drawable/robot_icon.png"
            } else {
                _avatarUrl.value = vm.getAvatarUrl(gameData.opponentUsername)
            }
        }
    }
    
    
    
    
}