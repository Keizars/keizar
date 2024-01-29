package org.keizar.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.keizar.game.serialization.GameSnapshot

val Context.savedGameStore by preferencesDataStore("saved-games")

interface SavedGameRepository {
    val lastGame: Flow<GameSnapshot?>
    suspend fun setLastGame(snapshot: GameSnapshot?)
}

internal class SavedGameRepositoryImpl(
    private val store: DataStore<Preferences>
) : SavedGameRepository {
    private companion object {
        val LAST_GAME = stringPreferencesKey("last-game")
    }

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override val lastGame: Flow<GameSnapshot?> = store.data.map { preferences ->
        preferences[LAST_GAME]?.let { json.decodeFromString(GameSnapshot.serializer(), it) }
    }

    override suspend fun setLastGame(snapshot: GameSnapshot?) {
        store.edit {
            if (snapshot == null) {
                it.remove(LAST_GAME)
            } else {
                it[LAST_GAME] = json.encodeToString(GameSnapshot.serializer(), snapshot)
            }
        }
    }
}