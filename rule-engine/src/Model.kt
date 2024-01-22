package org.keizar.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.keizar.game.serialization.PieceSnapshot

@Serializable
enum class Player {
    BLACK,
    WHITE;

    fun other(): Player = when (this) {
        BLACK -> WHITE
        WHITE -> BLACK
    }
}

@Serializable
enum class TileType {
    KING,
    QUEEN,
    BISHOP,
    KNIGHT,
    ROOK,
    KEIZAR,
    PLAIN,
}

@Serializable
enum class TileColor {
    BLACK,
    WHITE
}

@Serializable
class Move(
    val source: BoardPos,
    val dest: BoardPos,
    val isCapture: Boolean,
) {
    override fun toString(): String {
        return if (isCapture) "${source}x${dest}" else "${source}-${dest}"
    }
}

interface Piece {
    val index: Int
    val player: Player
    val pos: StateFlow<BoardPos>
    val isCaptured: StateFlow<Boolean>

    override fun toString(): String

    fun getSnapShot(): PieceSnapshot {
        return PieceSnapshot(
            index = index,
            player = player,
            pos = pos.value,
            isCaptured = isCaptured.value,
        )
    }
}

class MutablePiece(
    override val index: Int,
    override val player: Player,
    override val pos: MutableStateFlow<BoardPos>,
    override val isCaptured: MutableStateFlow<Boolean> = MutableStateFlow(false),
) : Piece {
    override fun toString(): String {
        return "Piece(index=$index, player=$player, pos=${pos.value}, isCaptured=${isCaptured.value})"
    }

    companion object {
        fun restore(snapshot: PieceSnapshot): MutablePiece {
            return MutablePiece(
                index = snapshot.index,
                player = snapshot.player,
                pos = MutableStateFlow(snapshot.pos),
                isCaptured = MutableStateFlow(snapshot.isCaptured),
            )
        }
    }
}

/**
 * Get a read-only view of this.
 */
fun MutablePiece.asPiece(): Piece = ReadOnlyPiece(this)

private class ReadOnlyPiece(
    private val piece: Piece,
) : Piece by piece