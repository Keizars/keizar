package org.keizar.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.internal.RuleEngineImpl
import org.keizar.game.serialization.RoundSnapshot

interface GameSession {
    val properties: BoardProperties

    val rounds: List<RoundSession>
    val currentRound: Flow<RoundSession>
    val currentRoundNo: StateFlow<Int>

    val finalWinner: Flow<GameResult?>

    fun currentRole(player: Player): StateFlow<Role>

    /**
     * Accumulated number of rounds this player has won.
     */
    fun wonRounds(player: Player): StateFlow<Int>

    /**
     * Accumulated number of pieces this player has captured.
     */
    fun capturedPieces(player: Player): Flow<Int>

    fun confirmNextTurn(player: Player)

    fun getSnapshot(): RoundSnapshot

    companion object {
        fun create(seed: Int? = null): GameSession {
            val properties = BoardProperties.getStandardProperties(seed)
            return create(properties)
        }

        fun create(properties: BoardProperties): GameSession {
            val ruleEngine = RuleEngineImpl(
                boardProperties = properties,
                ruleEngineCore = RuleEngineCoreImpl(properties),
            )
            return GameSessionImpl(properties) {
                RoundSessionImpl(ruleEngine)
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
    roundSessionConstructor: () -> RoundSession,
) : GameSession {
    override val rounds: List<RoundSession>

    override val currentRound: Flow<RoundSession>
    private val _currentRoundNo: MutableStateFlow<Int> = MutableStateFlow(0)
    override val currentRoundNo: StateFlow<Int> = _currentRoundNo.asStateFlow()
    override val finalWinner: Flow<GameResult?>
    private val haveWinner: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val curRoles: List<MutableStateFlow<Role>>
    private val wonRounds: List<MutableStateFlow<Int>>

    private val nextRoundAgreement: MutableList<Boolean>

    init {
        rounds = (1..properties.turns).map {
            roundSessionConstructor()
        }
        currentRound = currentRoundNo.map { rounds[it] }

        curRoles = listOf(
            MutableStateFlow(Role.WHITE),
            MutableStateFlow(Role.BLACK),
        )

        wonRounds = listOf(
            MutableStateFlow(0),
            MutableStateFlow(0),
        )

        nextRoundAgreement = mutableListOf(false, false)

        finalWinner = combine(
            haveWinner,
            wonRounds(Player.Player1),
            wonRounds(Player.Player2),
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

    override fun wonRounds(player: Player): StateFlow<Int> {
        return wonRounds[player.ordinal]
    }

    override fun capturedPieces(player: Player): Flow<Int> {
        return combine(rounds.map { it.getLostPiecesCount(currentRole(player).value) }) {
            it.sum()
        }
    }

    override fun confirmNextTurn(player: Player) {
        nextRoundAgreement[player.ordinal] = true
        if (nextRoundAgreement.all { it }) {
            proceedToNextTurn()
            nextRoundAgreement.forEachIndexed { index, _ -> nextRoundAgreement[index] = false }
        }
    }

    private fun proceedToNextTurn() {
        rounds[currentRoundNo.value].winner.value?.let { ++wonRounds[it.ordinal].value }
        if (currentRoundNo.value == properties.turns) {
            updateFinalWinner()
            return
        }
        ++_currentRoundNo.value
        curRoles.forEach { role -> role.value = role.value.other() }
    }

    private fun updateFinalWinner() {
        haveWinner.value = true
    }

    override fun getSnapshot(): RoundSnapshot {
        TODO("Not yet implemented")
    }
}

@Serializable
enum class Player {
    Player1,
    Player2,
}

@Serializable
sealed class GameResult {
    @Serializable
    data object Draw : GameResult()
    @Serializable
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
