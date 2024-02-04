package org.keizar.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.RoundSessionImpl
import org.keizar.game.serialization.GameSnapshot
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.game.Player
import kotlin.coroutines.CoroutineContext

interface RemoteGameSession : GameSession {

    val state: Flow<PlayerSessionState>

    val player: Player

    suspend fun waitUntilOpponentFound()

    fun close()

    companion object {
        // Create a standard RemoteGameSession using the seed provided.
        fun create(
            roomNumber: UInt,
            parentCoroutineContext: CoroutineContext,
            seed: Int? = null
        ): RemoteGameSession {
            val properties = BoardProperties.getStandardProperties(seed)
            return create(roomNumber, parentCoroutineContext, properties)
        }

        // Create a standard RemoteGameSession using the BoardProperties provided.
        fun create(
            roomNumber: UInt,
            parentCoroutineContext: CoroutineContext,
            properties: BoardProperties
        ): RemoteGameSession {
            val gameRoomClient = GameRoomClientImpl(roomNumber, parentCoroutineContext)
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
        fun restore(
            roomNumber: UInt,
            parentCoroutineContext: CoroutineContext,
            snapshot: GameSnapshot
        ): RemoteGameSession {
            val gameRoomClient = GameRoomClientImpl(roomNumber, parentCoroutineContext)
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

    override suspend fun waitUntilOpponentFound() {
        state.first { it == PlayerSessionState.PLAYING }
    }

    override fun close() {
        gameRoomClient.close()
    }

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