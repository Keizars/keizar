package org.keizar.client

import kotlinx.serialization.Serializable
import org.keizar.game.BoardProperties

@Serializable
data class GameRoom(
    val roomNumber: UInt,
    val gameProperties: BoardProperties,
    val numPlayers: Int,
)