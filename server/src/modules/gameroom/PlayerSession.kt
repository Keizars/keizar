package org.keizar.server.modules.gameroom

import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.UserInfo

interface PlayerSession {
    val session: StateFlow<DefaultWebSocketServerSession?>
    val state: StateFlow<PlayerSessionState>
    val user: UserInfo
    val username: String
    val isHost: Boolean
    val playerAllocation: Player
    fun setState(newState: PlayerSessionState)
    fun cancel(message: String)
    fun checkConnection(terminateOnDisconnected: Boolean = false)
    fun connect(newSession: DefaultWebSocketServerSession, recoverToState: PlayerSessionState)

    companion object {
        fun create(
            user: UserInfo,
            playerAllocation: Player,
            isHost: Boolean = false
        ): PlayerSession {
            return PlayerSessionImpl(user, playerAllocation, isHost)
        }
    }
}

class PlayerSessionImpl(
    override val user: UserInfo,
    override val playerAllocation: Player,
    override val isHost: Boolean,
) : PlayerSession {
    override var session: MutableStateFlow<DefaultWebSocketServerSession?> = MutableStateFlow(null)
    private val _state = MutableStateFlow(PlayerSessionState.STARTED)
    override val state: StateFlow<PlayerSessionState> = _state.asStateFlow()

    override val username: String get() = user.username

    override fun setState(newState: PlayerSessionState) {
        _state.value = newState
    }

    override fun checkConnection(terminateOnDisconnected: Boolean) {
        if (session.value?.isActive != true) {
            if (terminateOnDisconnected) {
                setState(PlayerSessionState.TERMINATING)
            } else {
                setState(PlayerSessionState.DISCONNECTED)
            }
            session.value = null
        }
    }

    override fun connect(
        newSession: DefaultWebSocketServerSession,
        recoverToState: PlayerSessionState,
    ) {
        session.value?.cancel("Session expired")
        session.value = newSession
        if (state.value == PlayerSessionState.DISCONNECTED) {
            setState(recoverToState)
        }

    }

    override fun cancel(message: String) {
        session.value?.cancel(message)
    }
}