package org.keizar.android.ui.game.mp

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.client.KeizarWebsocketClientFacade
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface MatchViewModel : HasBackgroundScope {
    @Stable
    val joinRoomIdEditing: StateFlow<String>

    fun setJoinRoomId(roomId: String)

    suspend fun joinRoom()


    @Stable
    val selfRoomId: StateFlow<String?>

    @Stable
    val creatingRoom: MutableStateFlow<Boolean>

    suspend fun createSelfRoom(): String

    fun removeSelfRoom()
}

fun MatchViewModel(): MatchViewModel = MatchViewModelImpl()

internal class MatchViewModelImpl : MatchViewModel, AbstractViewModel(), KoinComponent {
    private val keizarWebsocketClientFacade by inject<KeizarWebsocketClientFacade>()

    override val joinRoomIdEditing: MutableStateFlow<String> = MutableStateFlow("")

    override fun setJoinRoomId(roomId: String) {
        this.joinRoomIdEditing.value = roomId.filter { it.isDigit() }
    }

    override suspend fun joinRoom() {
        keizarWebsocketClientFacade.joinRoom(joinRoomIdEditing.value.toUInt())
    }

    override val selfRoomId: MutableStateFlow<String?> = MutableStateFlow(null)

    private val creatingRoomLock = Mutex()
    override val creatingRoom: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun createSelfRoom(): String {
        creatingRoomLock.withLock {
            selfRoomId.value?.let { return it }
            creatingRoom.value = true
            try {
                val roomId = keizarWebsocketClientFacade.createRoomAndJoin()
                selfRoomId.value = roomId.toString()
                return roomId.toString()
            } finally {
                creatingRoom.value = false
            }
        }
    }

    override fun removeSelfRoom() {
        selfRoomId.value = null
    }
}