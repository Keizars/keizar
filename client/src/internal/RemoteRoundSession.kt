package org.keizar.client.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.keizar.game.Role
import org.keizar.game.RoundSession
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.NeutralStats

internal sealed interface RemoteRoundSession : RoundSession

internal class RemoteRoundSessionImpl internal constructor(
    private val round: RoundSession,
    private val websocketHandler: GameSessionWsHandler,
) : RoundSession by round, RemoteRoundSession {
    init {
        websocketHandler.bind(this, round)
    }

    private val selfRole: StateFlow<Role> = websocketHandler.getCurrentSelfRole()

    /**
     * Returns false if the user tries to call [getAvailableTargets]
     * and [move] on their opponent's pieces
     */
    override fun getAvailableTargets(from: BoardPos): List<BoardPos> {
        if (round.pieceAt(from) != selfRole.value) return listOf()
        return round.getAvailableTargets(from)
    }

    override suspend fun move(from: BoardPos, to: BoardPos): Boolean {
        if (round.pieceAt(from) != selfRole.value) return false
        return round.move(from, to).also {
            if (it) websocketHandler.sendMove(from, to)
        }
    }

    override fun getNeutralStatistics(): NeutralStats {
        return round.getNeutralStatistics()
    }
}

