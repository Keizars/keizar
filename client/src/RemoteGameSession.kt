package org.keizar.client

import kotlinx.coroutines.flow.Flow
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.RoundSessionImpl
import org.keizar.game.serialization.GameSnapshot
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.game.Player

interface RemoteGameSession : GameSession {

    val state: Flow<PlayerSessionState>

    val player: Player

    companion object {
        // Create a standard RemoteGameSession using the seed provided.
        fun create(gameRoomClient: GameRoomClient, seed: Int? = null): RemoteGameSession {
            val properties = BoardProperties.getStandardProperties(seed)
            return create(gameRoomClient, properties)
        }

        // Create a standard RemoteGameSession using the BoardProperties provided.
        fun create(gameRoomClient: GameRoomClient, properties: BoardProperties): RemoteGameSession {
            val game = GameSession.create(properties) { ruleEngine ->
                RemoteRoundSessionImpl(
                    RoundSessionImpl(ruleEngine),
                    gameRoomClient
                )
            }
            gameRoomClient.bind(game)
            return RemoteGameSessionImpl(game, gameRoomClient)
        }

        // Restore a RemoteGameSession by a snapshot of the game.
        fun restore(gameRoomClient: GameRoomClient, snapshot: GameSnapshot): RemoteGameSession {
            val game = GameSession.restore(snapshot) { ruleEngine ->
                RemoteRoundSessionImpl(
                    RoundSessionImpl(ruleEngine),
                    gameRoomClient
                )
            }
            gameRoomClient.bind(game)
            return RemoteGameSessionImpl(game, gameRoomClient)
        }
    }
}

class RemoteGameSessionImpl(
    private val game: GameSession,
    private val gameRoomClient: GameRoomClient,
) : GameSession by game, RemoteGameSession {

    override val state: Flow<PlayerSessionState> = gameRoomClient.getPlayerState()
    // Note: Only call this when state has changed to PLAYING
    override val player: Player = gameRoomClient.getPlayer()
    override val rounds: List<RemoteRoundSession> = game.rounds.map { it as RemoteRoundSession }

    // Note: Only call this when state has changed to PLAYING
    override fun confirmNextRound(player: Player): Boolean {
        if (player != this.player) return false
        return game.confirmNextRound(player).also {
            if (it) gameRoomClient.sendConfirmNextRound()
        }
    }

    // Replaying is not allowed in online multiplayer mode
    override fun replayCurrentRound(): Boolean = false
    override fun replayGame(): Boolean = false
}