package org.keizar.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.keizar.game.BoardPos
import org.keizar.game.Role
import org.keizar.game.RoundSession

interface RemoteRoundSession: RoundSession

class RemoteRoundSessionImpl(
    private val round: RoundSession,
    gameRoomClient: GameRoomClient,
): RoundSession by round, RemoteRoundSession {
    override val curRole: StateFlow<Role> = gameRoomClient.getCurrentRole()

    override fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>> {
        if (round.pieceAt(from) != curRole.value) return flowOf(listOf())
        return round.getAvailableTargets(from)
    }

    override suspend fun move(from: BoardPos, to: BoardPos): Boolean {
        if (round.pieceAt(from) != curRole.value) return false
        return round.move(from, to)
    }
}

