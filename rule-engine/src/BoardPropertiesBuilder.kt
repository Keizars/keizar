package org.keizar.game

import org.keizar.game.tilearrangement.StandardTileArrangementFactory
import org.keizar.utils.communication.game.BoardPos

@DslMarker
annotation class BoardPropertiesDslMarker

@BoardPropertiesDslMarker
class BoardPropertiesBuilder private constructor(
    private var width: Int,
    private var height: Int,
    private var keizarTilePos: BoardPos,
    private var winningCount: Int,
    private var startingRole: Role,
    private var rounds: Int,
    private var piecesStartingPos: MutableMap<Role, List<BoardPos>>,
    private var tileArrangement: TileArrangementBuilder,
    instructions: BoardPropertiesBuilder.() -> Unit,
) {
    init {
        this.apply(instructions)
    }

    fun width(width: () -> Int) {
        this.width = width()
    }

    fun height(height: () -> Int) {
        this.height = height()
    }

    fun keizarTilePos(keizarTilePos: () -> BoardPos) {
        this.keizarTilePos = keizarTilePos()
    }

    fun winningCount(winningCount: () -> Int) {
        this.winningCount = winningCount()
    }

    fun startingRole(startingRole: () -> Role) {
        this.startingRole = startingRole()
    }

    fun roundsCount(rounds: () -> Int) {
        this.rounds = rounds()
    }

    fun piecesStartingPos(piecesStartingPos: () -> MutableMap<Role, List<BoardPos>>) {
        this.piecesStartingPos = piecesStartingPos()
    }

    fun tiles(tileArrangementInstructions: TileArrangementBuilder.() -> Unit = {}) {
        this.tileArrangement = TileArrangementBuilder(tileArrangementInstructions)
    }

    @BoardPropertiesDslMarker
    class TileArrangementBuilder(
        instructions: TileArrangementBuilder.() -> Unit = {},
    ) {
        private var seed: Int?
        private var tileArrangement: Map<BoardPos, TileType>

        init {
            seed = null
            tileArrangement = BoardPropertiesPrototypes.Plain.tileArrangement
            this.apply(instructions)
        }

        fun plain() {
            this.tileArrangement = BoardPropertiesPrototypes.Plain.tileArrangement
        }

        fun standard(seed: () -> Int) {
            this.seed = seed()
            this.tileArrangement =
                BoardPropertiesPrototypes.Standard(seed()).tileArrangement
        }

        fun prototype(prototype: () -> AbstractBoardProperties) {
            this.seed = prototype().seed
            this.tileArrangement = prototype().tileArrangement
        }

        fun factory(constraints: StandardTileArrangementFactory.() -> Unit) {
            this.tileArrangement = StandardTileArrangementFactory(constraints).build()
        }

        fun build(): Pair<Map<BoardPos, TileType>, Int?> {
            return Pair(tileArrangement, seed)
        }
    }

    constructor(
        prototype: AbstractBoardProperties,
        instructions: BoardPropertiesBuilder.() -> Unit = {},
    ) : this(
        width = prototype.width,
        height = prototype.height,
        keizarTilePos = prototype.keizarTilePos,
        winningCount = prototype.winningCount,
        startingRole = prototype.startingRole,
        rounds = prototype.rounds,
        piecesStartingPos = prototype.piecesStartingPos.toMutableMap(),
        tileArrangement = TileArrangementBuilder { prototype { prototype } },
        instructions = instructions,
    )

    fun build(): BoardProperties {
        val tileArrangement = tileArrangement.build()
        return BoardProperties(
            width = width,
            height = height,
            keizarTilePos = keizarTilePos,
            winningCount = winningCount,
            startingRole = startingRole,
            rounds = rounds,
            piecesStartingPos = piecesStartingPos,
            tileArrangement = tileArrangement.first,
            seed = tileArrangement.second,
        )
    }
}