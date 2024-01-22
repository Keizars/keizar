package org.keizar.game.serialization

import kotlinx.serialization.Serializable
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.Player

@Serializable
data class GameSnapshot(
    val properties: BoardProperties,
    val winningCounter: Int,
    val curPlayer: Player,
    val winner: Player?,
    val pieces: List<PieceSnapshot>,
)

@Serializable
data class PieceSnapshot(
    val index: Int,
    val player: Player,
    val pos: BoardPos,
    val isCaptured: Boolean,
)
