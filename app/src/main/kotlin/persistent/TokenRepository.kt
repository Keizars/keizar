package org.keizar.android.persistent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val KEY_TOKEN: Preferences.Key<String> = stringPreferencesKey("token")

class TokenRepository(
    private val store: DataStore<Preferences>
) {
    val token: Flow<String?> = store.data.map { it[KEY_TOKEN] }

    suspend fun setToken(token: String?) {
        store.edit {
            if (token == null) {
                it.remove(KEY_TOKEN)
            } else {
                it[KEY_TOKEN] = token
            }
        }
    }
}