package org.keizar.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.keizar.game.serialization.PieceSnapshot
import org.keizar.utils.communication.game.BoardPos

@Serializable
enum class Role {
    WHITE,
    BLACK;

    fun other(): Role = when (this) {
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
class Move(
    val source: BoardPos,
    val dest: BoardPos,
    val isCapture: Boolean,
) {
    override fun toString(): String {
        return if (isCapture) "${source}x${dest}" else "${source}-${dest}"
    }
}

@Serializable
data class MoveCountered(
    val move: Move,
    val counterValue: Int, // the value of the winning counter *before* the move
)

interface Piece {
    val index: Int
    val role: Role

    /**
     * The position of the piece on the board. It is always the logical position, i.e. seen as a [Role.WHITE].
     */
    val pos: StateFlow<BoardPos>
    val isCaptured: StateFlow<Boolean>

    override fun toString(): String

    fun getSnapShot(): PieceSnapshot {
        return PieceSnapshot(
            index = index,
            role = role,
            pos = pos.value,
            isCaptured = isCaptured.value,
        )
    }
}

class MutablePiece(
    override val index: Int,
    override val role: Role,
    override val pos: MutableStateFlow<BoardPos>,
    override val isCaptured: MutableStateFlow<Boolean> = MutableStateFlow(false),
) : Piece {
    override fun toString(): String {
        return "Piece(index=$index, player=$role, pos=${pos.value}, isCaptured=${isCaptured.value})"
    }

    companion object {
        fun restore(snapshot: PieceSnapshot): MutablePiece {
            return MutablePiece(
                index = snapshot.index,
                role = snapshot.role,
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