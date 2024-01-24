package org.keizar.game.serialization

import kotlinx.serialization.Serializable
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.Role

@Serializable
data class GameSnapshot(
    val properties: BoardProperties,
    val winningCounter: Int,
    val curRole: Role,
    val winner: Role?,
    val pieces: List<PieceSnapshot>,
)

@Serializable
data class PieceSnapshot(
    val index: Int,
    val role: Role,
    val pos: BoardPos,
    val isCaptured: Boolean,
)
