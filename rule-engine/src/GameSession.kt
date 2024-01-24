package org.keizar.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.internal.RuleEngineImpl
import org.keizar.game.serialization.GameSnapshot
import kotlin.random.Random

interface GameSession {
    val properties: BoardProperties

    val turns: List<TurnSession>
    val currentTurn: StateFlow<TurnSession>

    val finalWinner: StateFlow<Player?>

    fun currentRole(player: Player): Flow<Role>

    /**
     * Accumulated number of turns this player has won.
     */
    fun wonTurns(player: Player): Flow<Int>

    /**
     * Accumulated number of pieces this player has captured.
     */
    fun capturedPieces(player: Player): Flow<Int>

    fun getSnapshot(): GameSnapshot

    companion object {
        fun create(seed: Int? = null): GameSession {
            val properties = BoardProperties.getStandardProperties(seed)
            return create(properties)
        }

        fun create(random: Random): GameSession {
            val properties = BoardProperties.getStandardProperties(random)
            return create(properties)
        }

        fun create(properties: BoardProperties): GameSession {
            val ruleEngine = RuleEngineImpl(
                boardProperties = properties,
                ruleEngineCore = RuleEngineCoreImpl(properties),
            )
            return TurnSessionImpl(properties, ruleEngine)
        }

        fun restore(snapshot: GameSnapshot): GameSession {
            return TurnSessionImpl(
                properties = snapshot.properties,
                ruleEngine = RuleEngineImpl.restore(
                    gameSnapshot = snapshot,
                    ruleEngineCore = RuleEngineCoreImpl(snapshot.properties),
                )
            )
        }
    }
}

enum class Player {
    Player1,
    Player2,
}
