package org.keizar.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.keizar.client.modules.GameSessionModule
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.RoundSessionImpl
import org.keizar.game.snapshot.GameSnapshot
import org.keizar.utils.communication.PlayerSessionState
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.UserInfo
import kotlin.coroutines.CoroutineContext

interface RemoteGameSession : GameSession {

    val state: Flow<PlayerSessionState>

    val player: Flow<Player>

    suspend fun waitUntilOpponentFound()

    fun close()

    companion object {
        // use functions in KeizarClientFacade instead
        internal suspend fun createAndConnect(
            session: GameSessionModule,
        ): RemoteGameSession {
            session.connect()
            val game = GameSession.restore(session.getGameSnapshot()) { ruleEngine ->
                RemoteRoundSessionImpl(
                    RoundSessionImpl(ruleEngine),
                    session
                )
            }
            session.bind(game)
            return RemoteGameSessionImpl(
                game = game,
                gameSessionModule = session,
                player = session.getPlayer(),
            )
        }
    }
}

class RemoteGameSessionImpl internal constructor(
    private val game: GameSession,
    private val gameSessionModule: GameSessionModule,
    player: Player,
) : GameSession by game, RemoteGameSession {

    override val state: Flow<PlayerSessionState> = gameSessionModule.getPlayerState()

    override val player: Flow<Player> = MutableStateFlow(player)

    override suspend fun waitUntilOpponentFound() {
        state.first { it == PlayerSessionState.PLAYING }
    }

    override fun close() {
        gameSessionModule.close()
    }

    override val rounds: List<RemoteRoundSession> = game.rounds.map { it as RemoteRoundSession }

    override suspend fun confirmNextRound(player: Player): Boolean {
        if (player != this.player.first()) return false
        return game.confirmNextRound(player).also {
            if (it) gameSessionModule.sendConfirmNextRound()
        }
    }

    override fun replayCurrentRound(): Boolean = false
    override fun replayGame(): Boolean = false
}