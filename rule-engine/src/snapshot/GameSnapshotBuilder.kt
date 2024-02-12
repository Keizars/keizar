package org.keizar.game.snapshot

import org.keizar.game.AbstractBoardProperties
import org.keizar.game.BoardPropertiesBuilder
import org.keizar.game.BoardPropertiesPrototypes
import org.keizar.game.Role
import org.keizar.utils.communication.game.BoardPos

@DslMarker
annotation class GameSnapshotDslMarker

@GameSnapshotDslMarker
class GameSnapshotBuilder private constructor(
    private var properties: BoardPropertiesBuilder,
    private val rounds: MutableList<RoundSnapshotBuilder>,
    private var currentRoundNo: Int,
    instructions: GameSnapshotBuilder.() -> Unit
) {
    init {
        this.apply(instructions)
    }

    constructor(instructions: GameSnapshotBuilder.() -> Unit) : this(
        properties = BoardPropertiesBuilder(prototype = BoardPropertiesPrototypes.Plain),
        rounds = mutableListOf(),
        currentRoundNo = 0,
        instructions = instructions,
    )

    fun properties(
        prototype: AbstractBoardProperties = BoardPropertiesPrototypes.Plain,
        instructions: BoardPropertiesBuilder.() -> Unit,
    ) {
        properties = BoardPropertiesBuilder(prototype, instructions)
    }

    fun round(instructions: RoundSnapshotBuilder.() -> Unit): RoundSnapshotBuilder {
        val round = RoundSnapshotBuilder(instructions)
        rounds.add(round)
        return round
    }

    fun curRound(round: RoundSnapshotBuilder) {
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

    companion object {
        fun from(
            snapshot: GameSnapshot,
            instructions: GameSnapshotBuilder.() -> Unit = {},
        ): GameSnapshotBuilder {
            return GameSnapshotBuilder(
                BoardPropertiesBuilder(prototype = snapshot.properties),
                snapshot.rounds.map { RoundSnapshotBuilder.from(it) }.toMutableList(),
                snapshot.currentRoundNo,
                instructions
            )
        }
    }
}

@GameSnapshotDslMarker
class RoundSnapshotBuilder private constructor(
    private var winningCounter: Int,
    private var curRole: Role,
    private var winner: Role?,
    private var pieces: PiecesBuilder,
    instructions: RoundSnapshotBuilder.() -> Unit,
) {
    init {
        this.apply(instructions)
    }

    constructor(instructions: RoundSnapshotBuilder.() -> Unit) : this(
        winningCounter = 0,
        curRole = Role.WHITE,
        winner = null,
        pieces = PiecesBuilder(),
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
        this.pieces = PiecesBuilder { prototype(snapshot.pieces) }
    }

    fun pieces(instructions: PiecesBuilder.() -> Unit) {
        this.pieces = PiecesBuilder(instructions)
    }

    @GameSnapshotDslMarker
    class PiecesBuilder(
        instructions: PiecesBuilder.() -> Unit = {},
    ) {
        private var pieces: MutableList<PieceSnapshot>
        private var curIndex: Int

        init {
            pieces = mutableListOf()
            curIndex = 0
            this.apply(instructions)
        }

        fun prototype(prototype: List<PieceSnapshot>) {
            pieces = prototype.toMutableList()
            curIndex = prototype.lastIndex + 1
        }

        fun add(role: Role, pos: BoardPos, isCaptured: Boolean = false) {
            pieces.add(PieceSnapshot(curIndex++, role, pos, isCaptured))
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
            snapshot: RoundSnapshot,
            instructions: RoundSnapshotBuilder.() -> Unit = {},
        ): RoundSnapshotBuilder {
            return RoundSnapshotBuilder {
                prototype(snapshot)
                this.apply(instructions)
            }
        }
    }
}
