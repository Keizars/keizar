package org.keizar.game.snapshot

import org.keizar.game.AbstractBoardProperties
import org.keizar.game.BoardProperties
import org.keizar.game.BoardPropertiesBuilder
import org.keizar.game.BoardPropertiesPrototypes
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.utils.communication.game.BoardPos

@DslMarker
annotation class GameSnapshotDslMarker

/**
 * Builds a [GameSession] by building and restoring a [GameSnapshot] based on the initial [boardProperties] and applies [builderAction].
 */
inline fun buildGameSession(
    boardProperties: AbstractBoardProperties = BoardPropertiesPrototypes.Plain,
    builderAction: GameSnapshotBuilder.() -> Unit
): GameSession = GameSession.restore(buildGameSnapshot(boardProperties, builderAction))

/**
 * Builds a [GameSnapshot] based on the initial [boardProperties] and applies [builderAction].
 */
inline fun buildGameSnapshot(
    boardProperties: AbstractBoardProperties = BoardPropertiesPrototypes.Plain,
    builderAction: GameSnapshotBuilder.() -> Unit,
): GameSnapshot {
    return GameSnapshotBuilder(BoardPropertiesBuilder(boardProperties)).apply(builderAction).build()
}

@GameSnapshotDslMarker
class GameSnapshotBuilder @PublishedApi internal constructor(
    @PublishedApi internal var properties: BoardPropertiesBuilder,
    private val rounds: MutableList<RoundSnapshotBuilder> = mutableListOf(),
    private var currentRoundNo: Int = 0,
) {
    /**
     * Apply changes to current board properties
     */
    inline fun properties(
        instructions: BoardPropertiesBuilder.() -> Unit,
    ) {
        properties.apply(instructions)
    }

    /**
     * Adds a new round to the game, and configure the round using [instructions].
     *
     * The round is initialized with default pieces at their start positions just like in a real game.
     */
    fun round(instructions: RoundSnapshotBuilder.() -> Unit): RoundSnapshotBuilder {
        val round = RoundSnapshotBuilder(properties, instructions)
        rounds.add(round)
        return round
    }

    fun setCurRound(round: RoundSnapshotBuilder) {
        this.currentRoundNo = rounds.indexOfFirst { it == round }
        if (this.currentRoundNo == -1) error("Unexpected RoundSnapshotBuilder")
    }

    fun build(): GameSnapshot {
        return GameSnapshot(
            properties = properties.build(),
            rounds = rounds.map { it.build() },
            currentRoundNo = currentRoundNo
        )
    }

//    companion object {
//        fun from(
//            snapshot: GameSnapshot,
//            instructions: GameSnapshotBuilder.() -> Unit = {},
//        ): GameSnapshotBuilder {
//            return buildGameSnapshot(
//                BoardPropertiesBuilder(prototype = snapshot.properties),
//                snapshot.rounds.map { RoundSnapshotBuilder.from(snapshot.properties, it) }
//                    .toMutableList(),
//                snapshot.currentRoundNo,
//                instructions
//            )
//        }
//    }
}

@GameSnapshotDslMarker
class RoundSnapshotBuilder private constructor(
    private val properties: BoardPropertiesBuilder,
    private var winningCounter: Int,
    private var curRole: Role,
    private var winner: Role?,
    private var pieces: PiecesBuilder,
    instructions: RoundSnapshotBuilder.() -> Unit,
) {
    init {
        this.apply(instructions)
    }

    constructor(
        properties: BoardPropertiesBuilder,
        instructions: RoundSnapshotBuilder.() -> Unit
    ) : this(
        properties = properties,
        winningCounter = 0,
        curRole = Role.WHITE,
        winner = null,
        pieces = PiecesBuilder(properties),
        instructions = instructions,
    )

    fun build(): RoundSnapshot {
        return RoundSnapshot(winningCounter, curRole, winner, pieces.build())
    }

    fun winningCounter(winningCounter: () -> Int) {
        this.winningCounter = winningCounter()
    }

    fun curRole(curRole: () -> Role) {
        this.curRole = curRole()
    }

    fun winner(winner: () -> Role?) {
        this.winner = winner()
    }

    fun prototype(snapshot: RoundSnapshot) {
        this.winningCounter = snapshot.winningCounter
        this.curRole = snapshot.curRole
        this.winner = snapshot.winner
        this.pieces = PiecesBuilder(properties) { prototype(snapshot.pieces) }
    }

    /**
     * Remove all pieces in the round
     */
    fun resetPieces(instructions: PiecesBuilder.() -> Unit) {
        this.pieces = PiecesBuilder(properties, fromEmptyBoard = true, instructions)
    }

    fun setPieces(fromEmptyBoard: Boolean, instructions: PiecesBuilder.() -> Unit) {
        this.pieces = PiecesBuilder(properties, fromEmptyBoard, instructions)
    }

    @GameSnapshotDslMarker
    class PiecesBuilder(
        properties: BoardPropertiesBuilder,
        fromEmptyBoard: Boolean = false,
        instructions: PiecesBuilder.() -> Unit = {},
    ) {
        private var pieces: MutableList<PieceSnapshot>
        private var curIndex: Int

        init {
            val initialPoses = properties.getPiecesStartingPoses()
            pieces = mutableListOf()
            curIndex = 0
            if (!fromEmptyBoard) {
                initialPoses.forEach { (role, boardPoses) ->
                    boardPoses.forEach { add(role, it) }
                }
            }
            this.apply(instructions)
        }

        fun prototype(prototype: List<PieceSnapshot>) {
            pieces = prototype.toMutableList()
            curIndex = prototype.lastIndex + 1
        }

        fun clear() {
            pieces = mutableListOf()
            curIndex = 0
        }

        fun add(role: Role, pos: BoardPos, isCaptured: Boolean = false) {
            pieces.add(PieceSnapshot(curIndex++, role, pos, isCaptured))
        }

        fun white(pos: BoardPos, isCaptured: Boolean = false) {
            add(Role.WHITE, pos, isCaptured)
        }

        fun white(posStr: String, isCaptured: Boolean = false) {
            add(Role.WHITE, BoardPos(posStr), isCaptured)
        }

        fun black(pos: BoardPos, isCaptured: Boolean = false) {
            add(Role.BLACK, pos, isCaptured)
        }

        fun black(posStr: String, isCaptured: Boolean = false) {
            add(Role.BLACK, BoardPos(posStr), isCaptured)
        }

        fun remove(pos: BoardPos) {
            pieces.removeAll { it.pos == pos }
        }

        fun remove(posStr: String) {
            remove(BoardPos(posStr))
        }

        fun addAll(role: Role, poses: Collection<BoardPos>, isCaptured: Boolean = false) {
            poses.forEach { add(role, it, isCaptured) }
        }

        fun build(): List<PieceSnapshot> {
            return pieces
        }
    }

    companion object {
        fun from(
            properties: BoardProperties,
            snapshot: RoundSnapshot,
            instructions: RoundSnapshotBuilder.() -> Unit = {},
        ): RoundSnapshotBuilder {
            return RoundSnapshotBuilder(BoardPropertiesBuilder(properties)) {
                prototype(snapshot)
                this.apply(instructions)
            }
        }
    }
}
