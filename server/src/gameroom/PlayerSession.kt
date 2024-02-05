package org.keizar.server.gameroom

import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.keizar.utils.communication.PlayerSessionState

interface PlayerSession {
    val session: DefaultWebSocketServerSession
    val state: StateFlow<PlayerSessionState>
    fun setState(newState: PlayerSessionState)
}

class PlayerSessionImpl(override val session: DefaultWebSocketServerSession) : PlayerSession {
    private val _state = MutableStateFlow(PlayerSessionState.STARTED)
    override val state: StateFlow<PlayerSessionState> = _state.asStateFlow()

    override fun setState(newState: PlayerSessionState) {
        _state.value = newState
    }
}