package org.keizar.utils.communication.game

import kotlinx.serialization.Serializable

@Serializable
enum class Player {
    FirstWhitePlayer,
    FirstBlackPlayer;

    companion object {
        private val values = entries.toTypedArray()
        fun fromOrdinal(ordinal: Int): Player = values[ordinal]
    }
}