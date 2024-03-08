package org.keizar.android.ui.game.mp

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.ErrorMessage
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.client.services.RoomService
import org.keizar.game.BoardProperties
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random
import kotlin.random.nextUInt

interface MatchViewModel : HasBackgroundScope {
    @Stable
    val error: MutableStateFlow<ErrorMessage?>

    @Stable
    val joinRoomIdEditing: StateFlow<String>

    fun setJoinRoomId(roomId: String)

    suspend fun joinRoom()


    @Stable
    val selfRoomId: StateFlow<String?>

    @Stable
    val creatingRoom: MutableStateFlow<Boolean>

    suspend fun createSelfRoom()

    fun removeSelfRoom()
}

fun MatchViewModel(): MatchViewModel = MatchViewModelImpl()

internal class MatchViewModelImpl : MatchViewModel, AbstractViewModel(), KoinComponent {
    private val roomService: RoomService by inject()
    override val error = MutableStateFlow<ErrorMessage?>(null)

    override val joinRoomIdEditing: MutableStateFlow<String> = MutableStateFlow("")

    override fun setJoinRoomId(roomId: String) {
        this.joinRoomIdEditing.value = roomId.filter { it.isDigit() }
    }

    override suspend fun joinRoom() {
        roomService.joinRoom(joinRoomIdEditing.value)
    }

    override val selfRoomId: MutableStateFlow<String?> = MutableStateFlow(null)

    private val creatingRoomLock = Mutex()
    override val creatingRoom: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun createSelfRoom() {
        creatingRoomLock.withLock {
            selfRoomId.value?.let { return }
            creatingRoom.value = true
            try {
                val roomId = Random.nextUInt(10000u..99999u)
                roomService.createRoom(roomId.toString(), BoardProperties.getStandardProperties())
                selfRoomId.value = roomId.toString()
            } catch (e: Exception) {
                error.value = ErrorMessage.networkError()
                throw e
            } finally {
                creatingRoom.value = false
            }
        }
    }

    override fun removeSelfRoom() {
        selfRoomId.value = null
    }
}