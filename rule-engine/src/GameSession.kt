package org.keizar.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.keizar.game.internal.RuleEngine
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.internal.RuleEngineImpl
import org.keizar.game.serialization.GameSnapshot
import org.keizar.utils.communication.game.GameResult
import org.keizar.utils.communication.game.Player
import java.util.concurrent.atomic.AtomicInteger

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
    fun wonRounds(player: Player): Flow<Int>

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

    // Replay current round of the game. Reset the round state.
    // Can only be called in single player mode.
    fun replayCurrentRound(): Boolean

    // Replay the whole game. Change the currentRoundNo to 0, and reset the game state.
    // Can only be called in single player mode.
    fun replayGame(): Boolean

    fun getPlayer(role: Role, roundNo: Int): Player
    fun getRole(player: Player, roundNo: Int): Role
    fun getRoundWinner(roundNo: Int): Flow<Player?>

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

        // Create a standard GameSession using the BoardProperties
        // and a RoundSessionConstructor provided.
        fun create(
            properties: BoardProperties,
            roundSessionConstructor: (ruleEngine: RuleEngine) -> RoundSession,
        ): GameSession {
            return GameSessionImpl(properties) {
                val ruleEngine = RuleEngineImpl(
                    boardProperties = properties,
                    ruleEngineCore = RuleEngineCoreImpl(properties),
                )
                roundSessionConstructor(ruleEngine)
            }
        }

        // Restore a GameSession by a snapshot of the game.
        fun restore(snapshot: GameSnapshot): GameSession {
            return GameSessionImpl(
                properties = snapshot.properties,
                startFromRoundNo = snapshot.currentRoundNo,
            ) { index ->
                val ruleEngine = RuleEngineImpl.restore(
                    properties = snapshot.properties,
                    roundSnapshot = snapshot.rounds[index],
                    ruleEngineCore = RuleEngineCoreImpl(snapshot.properties),
                )
                RoundSessionImpl(ruleEngine)
            }
        }

        // Restore a GameSession by a snapshot of the game and a RoundSessionConstructor provided.
        fun restore(
            snapshot: GameSnapshot,
            roundSessionConstructor: (ruleEngine: RuleEngine) -> RoundSession,
        ): GameSession {
            return GameSessionImpl(
                properties = snapshot.properties,
                startFromRoundNo = snapshot.currentRoundNo,
            ) { index ->
                val ruleEngine = RuleEngineImpl.restore(
                    properties = snapshot.properties,
                    roundSnapshot = snapshot.rounds[index],
                    ruleEngineCore = RuleEngineCoreImpl(snapshot.properties),
                )
                roundSessionConstructor(ruleEngine)
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
    private val wonRounds: List<MutableStateFlow<List<Int>>>

    private val nextRoundAgreement: MutableList<Boolean>
    private val agreementCounter: AtomicInteger = AtomicInteger(0)

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
            MutableStateFlow(mutableListOf()),
            MutableStateFlow(mutableListOf()),
        )

        nextRoundAgreement = mutableListOf(false, false)

        finalWinner = combine(
            haveWinner,
            wonRounds(Player.FirstWhitePlayer),
            wonRounds(Player.FirstBlackPlayer),
            lostPieces(Player.FirstWhitePlayer),
            lostPieces(Player.FirstBlackPlayer),
        ) { haveWinner, player1Wins, player2Wins, player1LostPieces, player2LostPieces ->
            if (!haveWinner) {
                null
            } else if (player1Wins > player2Wins) {
                GameResult.Winner(Player.FirstWhitePlayer)
            } else if (player1Wins < player2Wins) {
                GameResult.Winner(Player.FirstBlackPlayer)
            } else if (player1LostPieces < player2LostPieces) {
                GameResult.Winner(Player.FirstWhitePlayer)
            } else if (player1LostPieces > player2LostPieces) {
                GameResult.Winner(Player.FirstBlackPlayer)
            } else {
                GameResult.Draw
            }
        }
    }

    override fun currentRole(player: Player): StateFlow<Role> {
        return curRoles[player.ordinal]
    }

    override fun wonRounds(player: Player): Flow<Int> {
        return wonRounds[player.ordinal].map { it.count() }
    }

    override fun lostPieces(player: Player): Flow<Int> {
        return combine(rounds.map { it.getLostPiecesCount(currentRole(player).value) }) {
            it.sum()
        }
    }

    override fun confirmNextRound(player: Player): Boolean {
        if (currentRoundNo.value >= properties.rounds) return false
        if (nextRoundAgreement[player.ordinal]) return false
        nextRoundAgreement[player.ordinal] = true
        agreementCounter.incrementAndGet()
        if (agreementCounter.compareAndSet(2, 0)) {
            proceedToNextTurn()
            nextRoundAgreement.replaceAll { false }
        }
        return true
    }

    private fun proceedToNextTurn() {
        val winningRole: Role? = rounds[currentRoundNo.value].winner.value
        val winningPlayer: Player? = winningRole?.let {
            if (currentRole(Player.FirstWhitePlayer).value == it) {
                Player.FirstWhitePlayer
            } else {
                Player.FirstBlackPlayer
            }
        }
        winningPlayer?.let { wonRounds[it.ordinal].value += currentRoundNo.value }
        if (currentRoundNo.value == properties.rounds - 1) {
            updateFinalWinner()
        }
        ++_currentRoundNo.value
        curRoles.forEach { role -> role.value = role.value.other() }
    }

    private fun updateFinalWinner() {
        haveWinner.value = true
    }

    override fun replayCurrentRound(): Boolean {
        rounds[_currentRoundNo.value].reset()
        nextRoundAgreement.replaceAll { false }
        agreementCounter.set(0)
        return true
    }

    override fun replayGame(): Boolean {
        rounds.forEach { it.reset() }
        nextRoundAgreement.replaceAll { false }
        agreementCounter.set(0)
        resetGameStatus()
        return true
    }

    private fun resetGameStatus() {
        curRoles[0].value = Role.WHITE
        curRoles[1].value = Role.BLACK
        wonRounds.forEach { it.value = mutableListOf() }
        _currentRoundNo.value = 0
        haveWinner.value = false
    }

    override fun getRoundWinner(roundNo: Int): Flow<Player?> {
        return rounds[roundNo].winner.map { it?.let { role -> getPlayer(role, roundNo) } }
    }

    override fun getRole(player: Player, roundNo: Int): Role {
        return if ((player.ordinal + roundNo) % 2 == 0) Role.WHITE else Role.BLACK
    }

    override fun getPlayer(role: Role, roundNo: Int): Player {
        return Player.fromOrdinal((role.ordinal + roundNo) % 2)
    }
}

