package org.keizar.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.internal.RuleEngineImpl
import org.keizar.game.serialization.GameSnapshot

/***
 * API for the backend. Representation of a complete game that may contain multiple rounds
 * on the same random-generated board.
 */
interface GameSession {
    // Game rules configuration
    val properties: BoardProperties

    // Sessions for different rounds
    val rounds: List<RoundSession>

    // Current round in progress.
    // Emits a new round after both player call confirmNextRound()
    val currentRound: Flow<RoundSession>

    // Round number of current round. starting from 0.
    // A currentRoundNo equal to properties.rounds indicates the whole game is
    // completed and finalWinner will emit a non-null GameResult.
    val currentRoundNo: StateFlow<Int>

    // The final winner. Emits a non-null value when the whole game ends.
    // Values could be GameResult.Winner(Player1/Player2) or GameResult.Draw.
    val finalWinner: Flow<GameResult?>

    // Returns the role (black/white) of the specified player in current round of game.
    fun currentRole(player: Player): StateFlow<Role>


    // Accumulated number of rounds this player has won.
    fun wonRounds(player: Player): StateFlow<Int>

    // Accumulated number of pieces this player has lost (been captured by the opponent).
    fun lostPieces(player: Player): Flow<Int>

    // The game will proceed to the next round only after both players call confirmNextRound().
    fun confirmNextRound(player: Player): Boolean

    // Return a serializable snapshot of the GameSession that can be restored to a GameSession
    // by GameSession.restore().
    fun getSnapshot(): GameSnapshot = GameSnapshot(
        properties = properties,
        rounds = rounds.map { it.getSnapshot() },
        currentRoundNo = currentRoundNo.value,
    )

    companion object {
        // Create a standard GameSession using the seed provided.
        fun create(seed: Int? = null): GameSession {
            val properties = BoardProperties.getStandardProperties(seed)
            return create(properties)
        }

        // Create a standard GameSession using the BoardProperties provided.
        fun create(properties: BoardProperties): GameSession {
            return GameSessionImpl(properties) {
                val ruleEngine = RuleEngineImpl(
                    boardProperties = properties,
                    ruleEngineCore = RuleEngineCoreImpl(properties),
                )
                RoundSessionImpl(ruleEngine)
            }
        }

        // Restore a GameSession by a snapshot of the game.
        fun restore(snapshot: GameSnapshot): GameSession {
            return GameSessionImpl(
                properties = snapshot.properties,
                startFromRoundNo = snapshot.currentRoundNo,
            ) { index ->
                RoundSessionImpl(
                    ruleEngine = RuleEngineImpl.restore(
                        properties = snapshot.properties,
                        roundSnapshot = snapshot.rounds[index],
                        ruleEngineCore = RuleEngineCoreImpl(snapshot.properties),
                    )
                )
            }
        }
    }
}

class GameSessionImpl(
    override val properties: BoardProperties,
    startFromRoundNo: Int = 0,
    roundSessionConstructor: (index: Int) -> RoundSession,
) : GameSession {
    override val rounds: List<RoundSession>

    override val currentRound: Flow<RoundSession>
    private val _currentRoundNo: MutableStateFlow<Int> = MutableStateFlow(startFromRoundNo)
    override val currentRoundNo: StateFlow<Int> = _currentRoundNo.asStateFlow()
    override val finalWinner: Flow<GameResult?>
    private val haveWinner: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val curRoles: List<MutableStateFlow<Role>>
    private val wonRounds: List<MutableStateFlow<Int>>

    private val nextRoundAgreement: MutableList<Boolean>

    init {
        rounds = (0..properties.rounds).map {
            // allocate one more RoundSession as a dummy session after the game ends
            roundSessionConstructor(it)
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
            lostPieces(Player.Player1),
            lostPieces(Player.Player2),
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

    override fun lostPieces(player: Player): Flow<Int> {
        return combine(rounds.map { it.getLostPiecesCount(currentRole(player).value) }) {
            it.sum()
        }
    }

    override fun confirmNextRound(player: Player): Boolean {
        if (currentRoundNo.value >= properties.rounds) return false
        nextRoundAgreement[player.ordinal] = true
        if (nextRoundAgreement.all { it }) {
            proceedToNextTurn()
            nextRoundAgreement.forEachIndexed { index, _ -> nextRoundAgreement[index] = false }
        }
        return true
    }

    private fun proceedToNextTurn() {
        val winningRole: Role? = rounds[currentRoundNo.value].winner.value
        val winningPlayer: Player? = winningRole?.let {
            if (currentRole(Player.Player1).value == it) Player.Player1 else Player.Player2
        }
        winningPlayer?.let { ++wonRounds[it.ordinal].value }
        if (currentRoundNo.value == properties.rounds - 1) {
            updateFinalWinner()
        }
        ++_currentRoundNo.value
        curRoles.forEach { role -> role.value = role.value.other() }
    }

    private fun updateFinalWinner() {
        haveWinner.value = true
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
