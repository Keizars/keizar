package org.keizar.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.internal.RuleEngineImpl
import org.keizar.game.serialization.GameSnapshot
import kotlin.random.Random

interface GameSession {
    val properties: BoardProperties

    val turns: List<TurnSession>
    val currentTurn: StateFlow<TurnSession>
    val currentTurnNo: StateFlow<Int>

    val finalWinner: Flow<GameResult?>

    fun currentRole(player: Player): StateFlow<Role>

    /**
     * Accumulated number of turns this player has won.
     */
    fun wonTurns(player: Player): StateFlow<Int>

    /**
     * Accumulated number of pieces this player has captured.
     */
    fun capturedPieces(player: Player): Flow<Int>

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
    private val _currentTurnNo: MutableStateFlow<Int> = MutableStateFlow(0)
    override val currentTurnNo: StateFlow<Int> = _currentTurnNo.asStateFlow()
    override val finalWinner: Flow<GameResult?>
    private val haveWinner: MutableStateFlow<Boolean> = MutableStateFlow(false)

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

        finalWinner = combine(
            haveWinner,
            wonTurns(Player.Player1),
            wonTurns(Player.Player2),
            capturedPieces(Player.Player1),
            capturedPieces(Player.Player2),
        ) { haveWinner, player1Wins, player2Wins, player1LostPieces, player2LostPieces ->
            if (!haveWinner) {
                null
            } else if (player1Wins > player2Wins) {
                GameResult.Winner(Player.Player1)
            } else if (player1Wins < player2Wins) {
                GameResult.Winner(Player.Player2)
            } else if (player1LostPieces < player2LostPieces) {
                GameResult.Winner(Player.Player1)
            } else if (player1LostPieces > player2LostPieces) {
                GameResult.Winner(Player.Player2)
            } else {
                GameResult.Draw
            }
        }
    }

    override fun currentRole(player: Player): StateFlow<Role> {
        return curRoles[player.ordinal]
    }

    override fun wonTurns(player: Player): StateFlow<Int> {
        return wonTurns[player.ordinal]
    }

    override fun capturedPieces(player: Player): Flow<Int> {
        return combine(turns.map { it.getLostPiecesCount(currentRole(player).value) }) {
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
        currentTurn.value.winner.value?.let { ++wonTurns[it.ordinal].value }
        if (currentTurnNo.value == properties.turns) {
            updateFinalWinner()
            return
        }
        ++_currentTurnNo.value
        _currentTurn.value = turns[currentTurnNo.value]
        curRoles.forEach { role -> role.value = role.value.other() }
    }

    private fun updateFinalWinner() {
        haveWinner.value = true
    }

    override fun getSnapshot(): GameSnapshot {
        TODO("Not yet implemented")
    }
}

enum class Player {
    Player1,
    Player2,
}

sealed class GameResult {
    data object Draw : GameResult()
    data class Winner(val player: Player) : GameResult()
    companion object {
        fun values(): Array<GameResult> {
            return arrayOf(Draw, Winner(Player.Player1), Winner(Player.Player2))
        }

        fun valueOf(value: String): GameResult {
            return when (value) {
                "DRAW" -> Draw
                "WINNER1" -> Winner(Player.Player1)
                "WINNER2" -> Winner(Player.Player2)
                else -> throw IllegalArgumentException("No object org.keizar.game.GameResult.$value")
            }
        }
    }
}
