package org.keizar.android.ui.game.mp

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.client.KeizarClientFacade
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface MatchViewModel : HasBackgroundScope {
    @Stable
    val joinRoomIdEditing: StateFlow<String>

    fun setJoinRoomId(roomId: String)


    @Stable
    val selfRoomId: StateFlow<String?>

    @Stable
    val creatingRoom: MutableStateFlow<Boolean>

    suspend fun createSelfRoom()

    fun removeSelfRoom()
}

fun MatchViewModel(): MatchViewModel = MatchViewModelImpl()

internal class MatchViewModelImpl : MatchViewModel, AbstractViewModel(), KoinComponent {
    private val keizarClientFacade by inject<KeizarClientFacade>()
    private val client = keizarClientFacade.createRoomClient()

    override val joinRoomIdEditing: MutableStateFlow<String> = MutableStateFlow("")

    override fun setJoinRoomId(roomId: String) {
        this.joinRoomIdEditing.value = roomId.filter { it.isDigit() }
    }

    override val selfRoomId: MutableStateFlow<String?> = MutableStateFlow(null)

    private val creatingRoomLock = Mutex()
    override val creatingRoom: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun createSelfRoom() {
        if (creatingRoomLock.isLocked || creatingRoom.value) return
        creatingRoomLock.withLock {
            if (creatingRoom.value) return
            creatingRoom.value = true
            try {
                selfRoomId.value = client.createRoom().roomNumber.toString()
            } finally {
                creatingRoom.value = false
            }
        }
    }

    override fun removeSelfRoom() {
        selfRoomId.value = null
    }
}