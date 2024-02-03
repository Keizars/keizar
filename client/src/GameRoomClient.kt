package org.keizar.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.keizar.game.GameSession
import org.keizar.game.Player
import org.keizar.game.Role
import org.keizar.utils.communication.PlayerSessionState

interface GameRoomClient {
    fun getCurrentRole(): StateFlow<Role>
    fun getPlayerState(): Flow<PlayerSessionState>
    fun getPlayer(): Player
    fun bind(it: GameSession)
}

class GameRoomClientImpl: GameRoomClient {
    override fun getCurrentRole(): StateFlow<Role> {
        TODO()
    }

    override fun getPlayerState(): Flow<PlayerSessionState> {
        TODO("Not yet implemented")
    }

    override fun getPlayer(): Player {
        TODO("Not yet implemented")
    }

    override fun bind(it: GameSession) {
        TODO("Not yet implemented")
    }
}