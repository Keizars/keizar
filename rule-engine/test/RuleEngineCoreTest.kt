package org.keizar.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.local.Piece
import org.keizar.game.local.Tile

class RuleEngineCoreTest {
    private val BoardPos.index get() = row * 8 + col

    private val ruleEngineCore = RuleEngineCoreImpl(BoardProperties.getStandardProperties())

    private val pieces1 = listOf(
        Piece(Player.WHITE, BoardPos("a1")),
        Piece(Player.WHITE, BoardPos("b2")),
        Piece(Player.WHITE, BoardPos("c3")),
        Piece(Player.WHITE, BoardPos("d2")),
        Piece(Player.WHITE, BoardPos("e1")),
        Piece(Player.WHITE, BoardPos("f1")),
        Piece(Player.WHITE, BoardPos("f2")),
        Piece(Player.WHITE, BoardPos("g4")),
        Piece(Player.WHITE, BoardPos("h1")),
        Piece(Player.BLACK, BoardPos("a8")),
        Piece(Player.BLACK, BoardPos("b7")),
        Piece(Player.BLACK, BoardPos("c6")),
        Piece(Player.BLACK, BoardPos("d7")),
        Piece(Player.BLACK, BoardPos("f5")),
        Piece(Player.BLACK, BoardPos("g5")),
        Piece(Player.BLACK, BoardPos("h5")),
        Piece(Player.WHITE, BoardPos("h7")),
        Piece(Player.WHITE, BoardPos("g8")),
        Piece(Player.BLACK, BoardPos("e5")),
    )

    private val board1: MutableList<Tile> = List(64) { Tile(TileType.PLAIN) }.toMutableList()

    init {
        for (piece in pieces1) {
            board1[piece.pos.index].piece = piece
        }
        board1[BoardPos("e2").index] = Tile(TileType.BISHOP)
        board1[BoardPos("e4").index] = Tile(TileType.QUEEN)
        board1[BoardPos("f3").index] = Tile(TileType.ROOK)
        board1[BoardPos("h3").index] = Tile(TileType.KNIGHT)
        board1[BoardPos("d5").index] = Tile(TileType.KEIZAR)
    }

    private val expectations = listOf(
        setOf(BoardPos("a2"), BoardPos("a3")),
        setOf(BoardPos("b3"), BoardPos("b4")),
        setOf(BoardPos("c4")),
        setOf(BoardPos("d3"), BoardPos("d4")),
        setOf(BoardPos("e2")),
        setOf(),
        setOf(BoardPos("f3")),
        setOf(BoardPos("f5"), BoardPos("h5")),
        setOf(BoardPos("h2"), BoardPos("h3")),
        setOf(BoardPos("a6"), BoardPos("a7")),
        setOf(BoardPos("b5"), BoardPos("b6")),
        setOf(BoardPos("c5")),
        setOf(BoardPos("d6")),
        setOf(BoardPos("f4"), BoardPos("g4")),
        setOf(),
        setOf(BoardPos("g4"), BoardPos("h4")),
        setOf(BoardPos("h8")),
        setOf(),
        setOf(BoardPos("e4")),
    )

    @TestFactory
    fun `test pawns movements`(): List<DynamicTest> {
        return pieces1.indices.map { i ->
            DynamicTest.dynamicTest("test pawns movement $i") {
                assertEquals(
                    expectations[i],
                    ruleEngineCore.showValidMoves(board1, pieces1[i]) { index }.toSet()
                )
            }
        }
    }
}