package org.keizar.client

import kotlinx.coroutines.flow.Flow
import org.keizar.client.internal.GameSessionWsHandler
import org.keizar.client.internal.RemoteRoundSession
import org.keizar.client.internal.RemoteRoundSessionImpl
import org.keizar.game.GameSession
import org.keizar.game.RoundSessionImpl
import org.keizar.game.snapshot.GameSnapshot
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.game.RoundStats

sealed interface RemoteGameSession : GameSession {
    /**
     * The player allocation of the user using this session:
     * either a [Player.FirstWhitePlayer] or a [Player.FirstBlackPlayer]
     */
    val player: Player

    companion object {
        internal suspend fun createAndConnect(
            gameSnapshot: GameSnapshot,
            websocketHandler: GameSessionWsHandler,
        ): RemoteGameSession {
            val game = GameSession.restore(gameSnapshot) { ruleEngine ->
                RemoteRoundSessionImpl(
                    RoundSessionImpl(ruleEngine),
                    websocketHandler
                )
            }
            websocketHandler.bind(game)
            websocketHandler.start()
            return RemoteGameSessionImpl(
                game = game,
                gameSessionWsHandler = websocketHandler,
            )
        }
    }
}

private class RemoteGameSessionImpl(
    private val game: GameSession,
    private val gameSessionWsHandler: GameSessionWsHandler,
) : GameSession by game, RemoteGameSession {
    override val rounds: List<RemoteRoundSession> = game.rounds.map { it as RemoteRoundSession }
    override val player: Player = gameSessionWsHandler.getSelfPlayer()

    /**
     * Returns false if the user tries to call [confirmNextRound] for their opponent
     */
    override suspend fun confirmNextRound(player: Player): Boolean {
        if (player != this.player) return false
        return game.confirmNextRound(player).also {
            if (it) gameSessionWsHandler.sendConfirmNextRound()
        }
    }

    override fun replayCurrentRound(): Boolean = false
    override fun replayGame(): Boolean = false

    override fun getRoundStats(roundNo: Int): Flow<RoundStats> {
        return game.getRoundStats(roundNo)
    }
}