package org.keizar.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.stateIn
import org.keizar.game.internal.RuleEngine
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.internal.RuleEngineImpl
import org.keizar.game.serialization.GameSnapshot
import java.lang.reflect.Constructor
import kotlin.random.Random

interface GameSession {
    val properties: BoardProperties

    val turns: List<TurnSession>
    val currentTurn: StateFlow<TurnSession>

    val finalWinner: StateFlow<Player?>

    fun currentRole(player: Player): StateFlow<Role>

    /**
     * Accumulated number of turns this player has won.
     */
    fun wonTurns(player: Player): StateFlow<Int>

    /**
     * Accumulated number of pieces this player has captured.
     */
    fun capturedPieces(player: Player): StateFlow<Int>

    fun confirmNextTurn(player: Player)

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
            return GameSessionImpl(properties) {
                TurnSessionImpl(ruleEngine)
            }
        }

//        fun restore(snapshot: GameSnapshot): GameSession {
//            return TurnSessionImpl(
//                properties = snapshot.properties,
//                ruleEngine = RuleEngineImpl.restore(
//                    gameSnapshot = snapshot,
//                    ruleEngineCore = RuleEngineCoreImpl(snapshot.properties),
//                )
//            )
//        }
    }
}

class GameSessionImpl(
    override val properties: BoardProperties,
    turnSessionConstructor: () -> TurnSession,
) : GameSession {
    override val turns: List<TurnSession>

    private val _currentTurn: MutableStateFlow<TurnSession>
    override val currentTurn: StateFlow<TurnSession>
    private val _finalWinner: MutableStateFlow<Player?> = MutableStateFlow(null)
    override val finalWinner: StateFlow<Player?> = _finalWinner.asStateFlow()

    private val curRoles: List<MutableStateFlow<Role>>
    private val wonTurns: List<MutableStateFlow<Int>>

    private val nextTurnAgreement: MutableList<Boolean>

    init {
        turns = (1..properties.turns).map {
            turnSessionConstructor()
        }
        _currentTurn = MutableStateFlow(turns[0])
        currentTurn = _currentTurn.asStateFlow()

        curRoles = listOf(
            MutableStateFlow(Role.WHITE),
            MutableStateFlow(Role.BLACK),
        )

        wonTurns = listOf(
            MutableStateFlow(0),
            MutableStateFlow(0),
        )

        nextTurnAgreement = mutableListOf(false, false)
    }

    override fun currentRole(player: Player): Flow<Role> {
        return curRoles[player.ordinal]
    }

    override fun wonTurns(player: Player): Flow<Int> {
        return wonTurns[player.ordinal]
    }

    override fun capturedPieces(player: Player): Flow<Int> {
        return combine(turns.map { it.getLostPiecesCount(curRoles[player.ordinal].value) }) {
            it.sum()
        }
    }

    override fun confirmNextTurn(player: Player) {
        nextTurnAgreement[player.ordinal] = true
        if (nextTurnAgreement.all { it }) {
            proceedToNextTurn()
            nextTurnAgreement.forEachIndexed { index, _ -> nextTurnAgreement[index] = false }
        }
    }

    private fun proceedToNextTurn() {
        TODO("Not yet implemented")
    }

    override fun getSnapshot(): GameSnapshot {
        TODO("Not yet implemented")
    }
}

enum class Player {
    Player1,
    Player2,
}
