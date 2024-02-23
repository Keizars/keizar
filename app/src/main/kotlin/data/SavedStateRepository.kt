package org.keizar.android.data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.game.snapshot.GameSnapshot

class SavedStateRepository(
    private val savedStateStore: DataStore<SavedState>
) {
    val savedState: Flow<SavedState> get() = savedStateStore.data

    suspend fun save(savedState: SavedState) {
        savedStateStore.updateData { savedState }
    }
}

@Serializable
sealed class SavedState {
    @Serializable
    data object Empty : SavedState()

    @Serializable
    data class SinglePlayerGame(
        val configuration: GameStartConfiguration,
        val snapshot: GameSnapshot,
    ) : SavedState()
}
