package org.keizar.android.ui.game.mp

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.keizar.android.ui.foundation.AbstractViewModel

interface MatchViewModel {
    @Stable
    val joinRoomIdEditing: StateFlow<String>

    fun setJoinRoomId(roomId: String)
    fun joinRoom()


    @Stable
    val selfRoomId: StateFlow<String?>

    fun createSelfRoom()
}

fun MatchViewModel(): MatchViewModel = MatchViewModelImpl()

internal class MatchViewModelImpl : MatchViewModel, AbstractViewModel() {
    override val joinRoomIdEditing: MutableStateFlow<String> = MutableStateFlow("")

    override fun setJoinRoomId(roomId: String) {
        this.joinRoomIdEditing.value = roomId.takeWhile { it.isDigit() }
    }

    override fun joinRoom() {
    }

    override val selfRoomId: MutableStateFlow<String?> = MutableStateFlow(null)

    override fun createSelfRoom() {
        selfRoomId.value = "123456"
    }
}