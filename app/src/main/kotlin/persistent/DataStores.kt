package org.keizar.android.persistent

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.keizar.android.data.SavedState
import java.io.InputStream
import java.io.OutputStream

val Context.savedStateStore by dataStore(
    fileName = "saved_state",
    serializer = object : Serializer<SavedState> {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        override val defaultValue: SavedState = SavedState.Empty

        override suspend fun readFrom(input: InputStream): SavedState {
            return json.decodeFromStream(SavedState.serializer(), input)
        }

        override suspend fun writeTo(t: SavedState, output: OutputStream) {
            return json.encodeToStream(SavedState.serializer(), t, output)
        }
    }
)
