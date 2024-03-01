package org.keizar.client.internal

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.keizar.client.RemoteRoundSession
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.game.RoundSession
import org.keizar.game.snapshot.GameSnapshot
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.message.ConfirmNextRound
import org.keizar.utils.communication.message.Move
import org.keizar.utils.communication.message.Respond
import kotlin.coroutines.CoroutineContext

internal interface GameSessionWsHandler : AutoCloseable {
    fun getCurrentSelfRole(): StateFlow<Role>
    fun getSelfPlayer(): Player
    fun getGameSnapshot(): GameSnapshot
    fun bind(session: GameSession)
    fun bind(remote: RemoteRoundSession, round: RoundSession)
    fun sendConfirmNextRound()
    fun sendMove(from: BoardPos, to: BoardPos)
    suspend fun start()
}

internal class GameSessionWsHandlerImpl(
    parentCoroutineContext: CoroutineContext,
    private val session: DefaultClientWebSocketSession,
    private val selfPlayer: Player,
    private val gameSnapshot: GameSnapshot,
) : GameSessionWsHandler {
    private val myCoroutineScope: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))

    private lateinit var gameSession: GameSession
    private val underlyingRoundSessionMap: MutableMap<RoundSession, RoundSession> = mutableMapOf()
    private val currentSelfRole: MutableStateFlow<Role> = MutableStateFlow(Role.WHITE)

    private val websocketSessionHandler =
        object : AbstractWebsocketSessionHandler(session, parentCoroutineContext) {
            override suspend fun processResponse(respond: Respond) {
                when (respond) {
                    ConfirmNextRound -> gameSession.confirmNextRound(selfPlayer.opponent())
                    is Move -> {
                        val round = gameSession.currentRound.first()
                        getUnderlyingRound(round).move(respond.from, respond.to)
                    }

                    else -> {
                        // ignore
                    }
                }
            }
        }

    override suspend fun start() {
        websocketSessionHandler.start()
    }

    override fun close() {
        websocketSessionHandler.close()
        myCoroutineScope.cancel()
    }

    private fun startUpdateCurRole() {
        myCoroutineScope.launch {
            val player = getSelfPlayer()
            gameSession.currentRoundNo.collect { curRoundNo ->
                currentSelfRole.value = gameSession.getRole(player, curRoundNo)
            }
        }
    }

    override fun getCurrentSelfRole(): StateFlow<Role> {
        return currentSelfRole
    }

    override fun getSelfPlayer(): Player {
        return selfPlayer
    }

    override fun getGameSnapshot(): GameSnapshot {
        return gameSnapshot
    }

    override fun bind(session: GameSession) {
        gameSession = session
        startUpdateCurRole()
    }

    override fun bind(remote: RemoteRoundSession, round: RoundSession) {
        underlyingRoundSessionMap[remote] = round
    }

    private fun getUnderlyingRound(remote: RoundSession): RoundSession {
        return underlyingRoundSessionMap[remote]!!
    }

    override fun sendConfirmNextRound() {
        websocketSessionHandler.sendRequest(ConfirmNextRound)
    }

    override fun sendMove(from: BoardPos, to: BoardPos) {
        websocketSessionHandler.sendRequest(Move(from, to))
    }
}