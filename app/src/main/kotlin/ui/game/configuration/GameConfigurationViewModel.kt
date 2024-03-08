package org.keizar.android.ui.game.configuration

import androidx.compose.runtime.Stable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.keizar.android.data.GameStartConfigurationEncoder
import org.keizar.android.data.SessionManager
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.Disposable
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.client.services.SeedBankService
import org.keizar.game.BoardProperties
import org.keizar.utils.communication.game.Difficulty
import org.keizar.game.Role
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface GameConfigurationViewModel : Disposable, HasBackgroundScope {
    /**
     * The current game configuration.
     */
    val configuration: StateFlow<GameStartConfiguration>

    /**
     * The current game configuration board seed.
     */
    val configurationSeed: Flow<String>

    /**
     * Update game configuration board seed with random seed.
     */
    fun updateRandomSeed()

    /**
     * The board properties of the current seed.
     */
    @Stable
    val boardProperties: SharedFlow<BoardProperties>

    /**
     * The rendered text of current game configuration board .
     */
    @Stable
    val configurationSeedText: StateFlow<String>

    /**
     * Change the rendered text of current game configuration board and update current board seed.
     */
    fun setConfigurationSeedText(value: String)

    /**
     * Whether the current game configuration board seed text has an error.
     */
    @Stable
    val isConfigurationSeedTextError: Flow<Boolean>

    /**
     * The role of the current player.
     * Role is either WHITE or BLACK.
     */
    @Stable
    val playAs: Flow<Role>

    /**
     * injected seed bank service
     */
    @Stable
    val seedBankService: SeedBankService

    /**
     * injected session manager service
     */
    @Stable
    val sessionManagerService: SessionManager

    /**
     * Set the role of the current player.
     */
    fun setPlayAs(role: Role)

    /**
     * The difficulty of the current game.
     * Difficulty is either EASY, MEDIUM, or HARD.
     */
    @Stable
    val difficulty: Flow<Difficulty>

    /**
     * Set the difficulty of the current game.
     */
    fun setDifficulty(difficulty: Difficulty)

    /**
     * Whether the fresh button is enabled.
     */
    @Stable
    val freshButtonEnable: Flow<Boolean>

    /**
     * Set the fresh button enable value.
     */
    fun setFreshButtonEnable(value: Boolean)

    fun setNewConfiguration(configuration: GameStartConfiguration)
}

fun GameConfigurationViewModel(
    initialConfiguration: GameStartConfiguration = GameStartConfiguration.random(),
): GameConfigurationViewModel = GameConfigurationViewModelImpl(initialConfiguration)

fun SinglePlayerGameConfigurationViewModel(
    initialConfiguration: GameStartConfiguration = GameStartConfiguration.random(),
): GameConfigurationViewModel = SinglePlayerGameConfigurationViewModelImpl(initialConfiguration)

@Serializable
data class GameStartConfiguration(
    @ProtoNumber(1) val layoutSeed: Int,
    @ProtoNumber(2) val playAs: Role,
    @ProtoNumber(3) val difficulty: Difficulty,
) {
    companion object {
        fun random(): GameStartConfiguration {
            return GameStartConfiguration(
                layoutSeed = BoardProperties.generateRandomSeed(),
                playAs = Role.entries.random(),
                difficulty = Difficulty.MEDIUM,
            )
        }
    }
}

fun GameStartConfiguration.createBoard(): BoardProperties =
    BoardProperties.getStandardProperties(layoutSeed)

private class GameConfigurationViewModelImpl(
    initialConfiguration: GameStartConfiguration = GameStartConfiguration.random(),
) : GameConfigurationViewModel, AbstractViewModel(), KoinComponent {
    override val configuration: MutableStateFlow<GameStartConfiguration> =
        MutableStateFlow(initialConfiguration)
    override val configurationSeed: Flow<String> =
        configuration.map { GameStartConfigurationEncoder.encode(it) }

    private val _configurationSeedText =
        MutableStateFlow(GameStartConfigurationEncoder.encode(initialConfiguration))
    override val configurationSeedText =
        merge(_configurationSeedText, configurationSeed).stateInBackground(
            GameStartConfigurationEncoder.encode(initialConfiguration)
        )

    private val layoutSeed: Flow<Int> = configuration.map { it.layoutSeed }
    override val playAs: Flow<Role> = configuration.map { it.playAs }
    override fun setPlayAs(role: Role) {
        updateConfiguration {
            copy(playAs = role)
        }
    }

    override val boardProperties: SharedFlow<BoardProperties> =
        layoutSeed.filterNotNull().map { layoutSeed ->
            BoardProperties.getStandardProperties(layoutSeed)
        }.shareInBackground()


    override val difficulty: Flow<Difficulty> = configuration.map { it.difficulty }

    override fun setDifficulty(difficulty: Difficulty) {
        updateConfiguration {
            copy(difficulty = difficulty)
        }
    }

    override val freshButtonEnable: MutableStateFlow<Boolean> = MutableStateFlow(true)

    override val seedBankService: SeedBankService by inject()

    override val sessionManagerService: SessionManager by inject()
    override fun setFreshButtonEnable(value: Boolean) {
        freshButtonEnable.value = value
    }

    override fun setNewConfiguration(configuration: GameStartConfiguration) {
        this.configuration.value = configuration
    }

    override fun setConfigurationSeedText(value: String) {
        backgroundScope.launch {
            setFreshButtonEnable(false)
            _configurationSeedText.value = value
            GameStartConfigurationEncoder.decode(value)?.let {
                updateConfiguration { it }
            }
            delay(3000)
            setFreshButtonEnable(true)
        }
    }

    override val isConfigurationSeedTextError: Flow<Boolean> = configurationSeedText.map {
        GameStartConfigurationEncoder.decode(it) == null
    }

    override fun updateRandomSeed() {
        backgroundScope.launch {
            setFreshButtonEnable(false)
            updateConfiguration {
                GameStartConfiguration.random()
            }
            delay(3000)
            setFreshButtonEnable(true)
        }
    }

    private inline fun updateConfiguration(block: GameStartConfiguration.() -> GameStartConfiguration) {
        this.configuration.value = this.configuration.value.block()
    }

}

private class SinglePlayerGameConfigurationViewModelImpl(
    initialConfiguration: GameStartConfiguration = GameStartConfiguration.random(),
) : GameConfigurationViewModel, AbstractViewModel(), KoinComponent {

    override val configuration: MutableStateFlow<GameStartConfiguration> =
        MutableStateFlow(initialConfiguration)
    override val configurationSeed: Flow<String> =
        configuration.map { GameStartConfigurationEncoder.encode(it) }

    private val _configurationSeedText =
        MutableStateFlow(GameStartConfigurationEncoder.encode(initialConfiguration))
    override val configurationSeedText =
        merge(_configurationSeedText, configurationSeed).stateInBackground(
            GameStartConfigurationEncoder.encode(initialConfiguration)
        )

    private val layoutSeed: Flow<Int> = configuration.map { it.layoutSeed }
    override val playAs: Flow<Role> = configuration.map { it.playAs }
    override fun setPlayAs(role: Role) {
        updateConfiguration {
            copy(playAs = role)
        }
    }

    override val boardProperties: SharedFlow<BoardProperties> =
        layoutSeed.filterNotNull().map { layoutSeed ->
            BoardProperties.getStandardProperties(layoutSeed)
        }.shareInBackground()


    override val difficulty: Flow<Difficulty> = configuration.map { it.difficulty }

    override fun setDifficulty(difficulty: Difficulty) {
        updateConfiguration {
            copy(difficulty = difficulty)
        }
    }

    override val freshButtonEnable: MutableStateFlow<Boolean> = MutableStateFlow(true)

    override val seedBankService: SeedBankService by inject()

    override val sessionManagerService: SessionManager by inject()
    override fun setFreshButtonEnable(value: Boolean) {
        freshButtonEnable.value = value
    }

    override fun setNewConfiguration(configuration: GameStartConfiguration) {
        TODO("Not yet implemented")
    }

    override fun setConfigurationSeedText(value: String) {
        _configurationSeedText.value = value
        GameStartConfigurationEncoder.decode(value)?.let {
            updateConfiguration { it }
        }
    }

    override fun updateRandomSeed() {
        updateConfiguration {
            GameStartConfiguration.random()
        }
    }

    override val isConfigurationSeedTextError: Flow<Boolean> = configurationSeedText.map {
        GameStartConfigurationEncoder.decode(it) == null
    }


    private inline fun updateConfiguration(block: GameStartConfiguration.() -> GameStartConfiguration) {
        this.configuration.value = this.configuration.value.block()
    }
}