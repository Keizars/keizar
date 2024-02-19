package org.keizar.game.snapshot

import kotlinx.serialization.Serializable
import org.keizar.game.BoardProperties
import org.keizar.game.Role
import org.keizar.utils.communication.game.BoardPos

@Serializable
data class GameSnapshot(
    val properties: BoardProperties,
    val rounds: List<RoundSnapshot>,
    val currentRoundNo: Int,
)

@Serializable
data class RoundSnapshot(
    val winningCounter: Int,
    val curRole: Role,
    val winner: Role?,
    val pieces: List<PieceSnapshot>,
    val isFreeMove: Boolean = false,
    val disableWinner: Boolean = false,
)

@Serializable
data class PieceSnapshot(
    val index: Int,
    val role: Role,
    val pos: BoardPos,
    val isCaptured: Boolean,
)
