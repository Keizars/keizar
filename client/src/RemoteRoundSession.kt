package org.keizar.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.keizar.client.modules.GameSessionModule
import org.keizar.game.Role
import org.keizar.game.RoundSession
import org.keizar.utils.communication.game.BoardPos

interface RemoteRoundSession: RoundSession

class RemoteRoundSessionImpl internal constructor(
    private val round: RoundSession,
    private val gameSessionModule: GameSessionModule,
): RoundSession by round, RemoteRoundSession {
    // Note: Only call this when state has changed to PLAYING
    override val curRole: StateFlow<Role> = gameSessionModule.getCurrentRole()

    // Note: Only call this when state has changed to PLAYING
    override fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>> {
        if (round.pieceAt(from) != curRole.value) return flowOf(listOf())
        return round.getAvailableTargets(from)
    }

    // Note: Only call this when state has changed to PLAYING
    override suspend fun move(from: BoardPos, to: BoardPos): Boolean {
//        if (round.pieceAt(from) != curRole.value) return false
        return round.move(from, to).also {
            if (it) gameSessionModule.sendMove(from, to)
        }
    }
}

