package org.keizar.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.keizar.client.modules.GameRoomInfo
import org.keizar.client.modules.GameSessionModule
import org.keizar.client.modules.GameSessionModuleImpl
import org.keizar.client.modules.KeizarHttpClient
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
            parentCoroutineContext: CoroutineContext,
            session: GameSessionModule,
            properties: BoardProperties,
            userInfo: UserInfo,
        ): RemoteGameSession {
            val game = GameSession.create(properties) { ruleEngine ->
                RemoteRoundSessionImpl(
                    RoundSessionImpl(ruleEngine),
                    session
                )
            }
            session.bind(game)
            session.connect(userInfo)
            return RemoteGameSessionImpl(game, session, parentCoroutineContext)
        }

        internal suspend fun restoreAndConnect(
            parentCoroutineContext: CoroutineContext,
            session: GameSessionModule,
            snapshot: GameSnapshot,
            userInfo: UserInfo,
        ): RemoteGameSession {
            val game = GameSession.restore(snapshot) { ruleEngine ->
                RemoteRoundSessionImpl(
                    RoundSessionImpl(ruleEngine),
                    session
                )
            }
            session.bind(game)
            session.connect(userInfo)
            return RemoteGameSessionImpl(game, session, parentCoroutineContext)
        }
    }
}

class RemoteGameSessionImpl internal constructor(
    private val game: GameSession,
    private val gameSessionModule: GameSessionModule,
    parentCoroutineContext: CoroutineContext,
) : GameSession by game, RemoteGameSession {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    override val state: Flow<PlayerSessionState> = gameSessionModule.getPlayerState()

    override val player: Flow<Player> = MutableSharedFlow<Player>(replay = 1).apply {
        myCoroutineScope.launch {
            emit(gameSessionModule.getPlayer())
        }
    }

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