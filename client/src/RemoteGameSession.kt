package org.keizar.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        suspend fun createAndConnect(
            room: GameRoom,
            parentCoroutineContext: CoroutineContext,
        ): RemoteGameSession {
            val gameRoomClient = GameSessionClientImpl(room.roomNumber, parentCoroutineContext)
            val game = GameSession.create(room.gameProperties) { ruleEngine ->
                RemoteRoundSessionImpl(
                    RoundSessionImpl(ruleEngine),
                    gameRoomClient
                )
            }
            gameRoomClient.bind(game)
            gameRoomClient.connect()
            return RemoteGameSessionImpl(game, gameRoomClient)
        }

        // Restore a RemoteGameSession by a snapshot of the game.
        suspend fun restoreAndConnect(
            room: GameRoom,
            parentCoroutineContext: CoroutineContext,
            snapshot: GameSnapshot,
        ): RemoteGameSession {
            val gameRoomClient = GameSessionClientImpl(room.roomNumber, parentCoroutineContext)
            val game = GameSession.restore(snapshot) { ruleEngine ->
                RemoteRoundSessionImpl(
                    RoundSessionImpl(ruleEngine),
                    gameRoomClient
                )
            }
            gameRoomClient.bind(game)
            gameRoomClient.connect()
            return RemoteGameSessionImpl(game, gameRoomClient)
        }
    }
}

class RemoteGameSessionImpl internal constructor(
    private val game: GameSession,
    private val gameSessionClient: GameSessionClient,
) : GameSession by game, RemoteGameSession {

    override val state: Flow<PlayerSessionState> = gameSessionClient.getPlayerState()

    // Note: Only call this when state has changed to PLAYING
    override val player: Player = gameSessionClient.getPlayer()

    override suspend fun waitUntilOpponentFound() {
        state.first { it == PlayerSessionState.PLAYING }
    }

    override fun close() {
        gameSessionClient.close()
    }

    override val rounds: List<RemoteRoundSession> = game.rounds.map { it as RemoteRoundSession }

    // Note: Only call this when state has changed to PLAYING
    override fun confirmNextRound(player: Player): Boolean {
        if (player != this.player) return false
        return game.confirmNextRound(player).also {
            if (it) gameSessionClient.sendConfirmNextRound()
        }
    }

    // Replaying is not allowed in online multiplayer mode
    override fun replayCurrentRound(): Boolean = false
    override fun replayGame(): Boolean = false
}