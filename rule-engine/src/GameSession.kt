package org.keizar.game

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.local.RuleEngine
import org.keizar.game.local.RuleEngineImpl
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

interface GameSession {
    val properties: BoardProperties

    fun pieceAt(pos: BoardPos): Flow<Player?>
    val winner: StateFlow<Player?>
    val winningCounter: StateFlow<Int>
    val curPlayer: StateFlow<Player>

    suspend fun undo(): Boolean
    suspend fun redo(): Boolean

    fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>>
    suspend fun move(from: BoardPos, to: BoardPos): Boolean

    companion object {
        fun create(random: Random = Random): GameSession {
            val properties = BoardProperties.getStandardProperties(random)
            val ruleEngine = RuleEngineImpl(
                boardProperties = properties,
                ruleEngineCore = RuleEngineCoreImpl(properties),
            )
            return GameSessionImpl(properties, ruleEngine)
        }
    }
}

class GameSessionImpl(
    override val properties: BoardProperties,
    private val ruleEngine: RuleEngine,
) : GameSession {
    @Suppress("OPT_IN_USAGE")
    override fun pieceAt(pos: BoardPos): StateFlow<Player?> {
        return flow {
            while (true) {
                emit(ruleEngine.pieceAt(pos))
                kotlinx.coroutines.delay(0.1.seconds)
            }
        }.stateIn(GlobalScope, started = SharingStarted.WhileSubscribed(), null)
    }

    override val winner: StateFlow<Player?> = ruleEngine.winner

    override val winningCounter: StateFlow<Int> = ruleEngine.winningCounter

    override val curPlayer: StateFlow<Player> = ruleEngine.curPlayer

    override suspend fun undo(): Boolean {
        // TODO("Not yet implemented")
        return true
    }

    override suspend fun redo(): Boolean {
        // TODO("Not yet implemented")
        return true
    }

    override fun getAvailableTargets(from: BoardPos): Flow<List<BoardPos>> {
        return flowOf(ruleEngine.showPossibleMoves(from))
    }

    override suspend fun move(from: BoardPos, to: BoardPos): Boolean {
        return ruleEngine.move(from, to)
    }
}
