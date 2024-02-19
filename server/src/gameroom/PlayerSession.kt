package org.keizar.server.gameroom

import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.message.UserInfo

interface PlayerSession {
    val session: DefaultWebSocketServerSession
    val state: StateFlow<PlayerSessionState>
    val user: UserInfo
    fun setState(newState: PlayerSessionState)
}

class PlayerSessionImpl(
    override val session: DefaultWebSocketServerSession,
    override val user: UserInfo
) : PlayerSession {
    private val _state = MutableStateFlow(PlayerSessionState.STARTED)
    override val state: StateFlow<PlayerSessionState> = _state.asStateFlow()

    override fun setState(newState: PlayerSessionState) {
        _state.value = newState
    }
}