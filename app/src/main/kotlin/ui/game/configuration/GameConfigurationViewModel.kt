package org.keizar.android.ui.game.configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.keizar.android.GameStartConfigurationEncoder
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.game.BoardProperties
import org.keizar.game.Difficulty
import org.keizar.game.Role

interface GameConfigurationViewModel {
    val configuration: StateFlow<GameStartConfiguration>
    val configurationSeed: Flow<String>
    fun updateRandomSeed()

    @Stable
    val layoutSeed: Flow<Int?>

    @Stable
    val boardProperties: SharedFlow<BoardProperties>

    @Stable
    val configurationSeedText: StateFlow<String>
    fun setConfigurationSeedText(value: String)

    @Stable
    val playAs: Flow<Role?>
    fun setPlayAs(role: Role?)

    @Stable
    val difficulty: Flow<Difficulty>
    fun setDifficulty(difficulty: Difficulty)
}

@Composable
fun rememberGameConfigurationViewModel(): GameConfigurationViewModel = GameConfigurationViewModelImpl()

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
                difficulty = Difficulty.EASY,
            )
        }
    }
}

fun GameStartConfiguration.createBoard(): BoardProperties =
    BoardProperties.getStandardProperties(layoutSeed)

private class GameConfigurationViewModelImpl : GameConfigurationViewModel, AbstractViewModel() {
    override val configuration: MutableStateFlow<GameStartConfiguration> =
        MutableStateFlow(GameStartConfiguration.random())
    override val configurationSeed: Flow<String> = configuration.map { GameStartConfigurationEncoder.encode(it) }

    private val _configurationSeedText = MutableStateFlow(configuration.value.layoutSeed.toString())
    override val configurationSeedText = merge(_configurationSeedText, configurationSeed).stateInBackground("")
    override val layoutSeed: Flow<Int?> = configuration.map { it.layoutSeed }
    override val playAs: Flow<Role?> = configuration.map { it.playAs }
    override fun setPlayAs(role: Role?) {
        updateConfiguration {
            copy(playAs = role ?: Role.WHITE)
        }
    }

    override val boardProperties: SharedFlow<BoardProperties> =
        combine(layoutSeed, playAs) { layoutSeed, playAs ->
            BoardProperties.getStandardProperties(
                layoutSeed ?: 0
            )
        }.shareInBackground()


    override val difficulty: Flow<Difficulty> = configuration.map { it.difficulty }

    override fun setDifficulty(difficulty: Difficulty) {
        updateConfiguration {
            copy(difficulty = difficulty)
        }
    }

    override fun setConfigurationSeedText(value: String) {
        _configurationSeedText.value = value
        value.toIntOrNull()?.let { seed ->
            updateConfiguration {
                copy(layoutSeed = seed)
            }
        }
    }

    override fun updateRandomSeed() {
        val newSeed = BoardProperties.generateRandomSeed()
        _configurationSeedText.value = newSeed.toString()
        updateConfiguration {
            GameStartConfiguration.random()
        }
    }

    private inline fun updateConfiguration(block: GameStartConfiguration.() -> GameStartConfiguration) {
        this.configuration.value = this.configuration.value.block()
    }

}
