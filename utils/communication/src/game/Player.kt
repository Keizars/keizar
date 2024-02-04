package org.keizar.utils.communication.game

import kotlinx.serialization.Serializable

@Serializable
enum class Player {
    FirstWhitePlayer,
    FirstBlackPlayer;

    fun opponent(): Player {
        return when (this) {
            FirstWhitePlayer -> FirstBlackPlayer
            FirstBlackPlayer -> FirstWhitePlayer
        }
    }

    companion object {
        private val values = entries.toTypedArray()
        fun fromOrdinal(ordinal: Int): Player = values[ordinal]
    }
}