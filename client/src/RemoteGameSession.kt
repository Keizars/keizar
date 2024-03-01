package org.keizar.client

import org.keizar.client.internal.GameSessionWsHandler
import org.keizar.game.GameSession
import org.keizar.game.RoundSessionImpl
import org.keizar.game.snapshot.GameSnapshot
import org.keizar.utils.communication.game.Player

interface RemoteGameSession : GameSession {
    // Self player
    val player: Player

    companion object {
        // use functions in KeizarClientFacade instead
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

class RemoteGameSessionImpl internal constructor(
    private val game: GameSession,
    private val gameSessionWsHandler: GameSessionWsHandler,
) : GameSession by game, RemoteGameSession {
    override val rounds: List<RemoteRoundSession> = game.rounds.map { it as RemoteRoundSession }
    override val player: Player = gameSessionWsHandler.getSelfPlayer()

    override suspend fun confirmNextRound(player: Player): Boolean {
        if (player != this.player) return false
        return game.confirmNextRound(player).also {
            if (it) gameSessionWsHandler.sendConfirmNextRound()
        }
    }

    override fun replayCurrentRound(): Boolean = false
    override fun replayGame(): Boolean = false
}