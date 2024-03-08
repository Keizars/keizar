package org.keizar.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.internal.Tile
import org.keizar.utils.communication.game.BoardPos

class RuleEngineCoreTest {
    private val BoardPos.index get() = row * 8 + col

    private val ruleEngineCore = RuleEngineCoreImpl(BoardProperties.getStandardProperties())

    /***
     *   8 p _ _ _ _ _ P _
     *   7 _ p p p p _ _ P
     *   6 _ _ _ _ _ _ _ _
     *   5 _ _ _ k _ p p p
     *   4 _ _ _ _ * _ P _
     *   3 _ _ P _ _ * _ *
     *   2 _ P _ P * P _ _
     *   1 P _ _ _ P P _ P
     *     a b c d e f g h
     */
    private var index1 = 0
    private val pieces1 = listOf(
        Pair(Role.WHITE, BoardPos("a1")),
        Pair(Role.WHITE, BoardPos("b2")),
        Pair(Role.WHITE, BoardPos("c3")),
        Pair(Role.WHITE, BoardPos("d2")),
        Pair(Role.WHITE, BoardPos("e1")),
        Pair(Role.WHITE, BoardPos("f1")),
        Pair(Role.WHITE, BoardPos("f2")),
        Pair(Role.WHITE, BoardPos("g4")),
        Pair(Role.WHITE, BoardPos("h1")),
        Pair(Role.BLACK, BoardPos("a8")),
        Pair(Role.BLACK, BoardPos("b7")),
        Pair(Role.BLACK, BoardPos("c7")),
        Pair(Role.BLACK, BoardPos("d7")),
        Pair(Role.BLACK, BoardPos("f5")),
        Pair(Role.BLACK, BoardPos("g5")),
        Pair(Role.BLACK, BoardPos("h5")),
        Pair(Role.WHITE, BoardPos("h7")),
        Pair(Role.WHITE, BoardPos("g8")),
        Pair(Role.BLACK, BoardPos("e7")),
        Pair(Role.BLACK, BoardPos("d5")),
    ).map { (player, pos) ->
        MutablePiece(index1++, player, MutableStateFlow(pos))
    }

    private val board1: List<Tile> = run {
        val board = List(64) { Tile(TileType.PLAIN) }.toMutableList()
        board[BoardPos("e2").index] = Tile(TileType.BISHOP)
        board[BoardPos("e4").index] = Tile(TileType.QUEEN)
        board[BoardPos("f3").index] = Tile(TileType.ROOK)
        board[BoardPos("h3").index] = Tile(TileType.KNIGHT)
        board[BoardPos("d5").index] = Tile(TileType.KEIZAR)
        for (piece in pieces1) {
            board[piece.pos.value.index].piece = piece
        }
        board
    }

    private val expectations1 = listOf(
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
        setOf(BoardPos("c6"), BoardPos("c5")),
        setOf(BoardPos("d6")),
        setOf(BoardPos("f4"), BoardPos("g4")),
        setOf(),
        setOf(BoardPos("g4"), BoardPos("h4")),
        setOf(BoardPos("h8")),
        setOf(),
        setOf(BoardPos("e6"), BoardPos("e5")),
        setOf()
    )

    @TestFactory
    fun `test pawns movements`(): List<DynamicTest> {
        return pieces1.indices.map { i ->
            DynamicTest.dynamicTest("test pawns movement $i") {
                assertEquals(
                    expectations1[i],
                    ruleEngineCore.showValidMoves(board1, pieces1[i]) { index }.toSet()
                )
            }
        }
    }

    /***
     *   8 _ _ _ k _ _ _ _
     *   7 _ _ b _ _ _ q _
     *   6 _ B _ _ _ _ _ _
     *   5 _ B _ * _ _ _ _
     *   4 _ _ _ _ _ _ _ _
     *   3 _ _ _ Q K _ _ _
     *   2 _ R _ r _ _ _ _
     *   1 _ _ _ _ _ N _ _
     *     a b c d e f g h
     */
    private var index2 = 0
    private val pieces2 = listOf(
        Pair(Role.WHITE, BoardPos("b2")),
        Pair(Role.WHITE, BoardPos("b5")),
        Pair(Role.WHITE, BoardPos("b6")),
        Pair(Role.BLACK, BoardPos("c7")),
        Pair(Role.BLACK, BoardPos("d2")),
        Pair(Role.WHITE, BoardPos("d3")),
        Pair(Role.BLACK, BoardPos("d8")),
        Pair(Role.WHITE, BoardPos("e3")),
        Pair(Role.BLACK, BoardPos("g7")),
        Pair(Role.WHITE, BoardPos("f1")),
    ).map { (player, pos) ->
        MutablePiece(index2++, player, MutableStateFlow(pos))
    }

    private val board2 = run {
        val board = List(64) { Tile(TileType.PLAIN) }.toMutableList()
        board[BoardPos("b2").index] = Tile(TileType.ROOK)
        board[BoardPos("b5").index] = Tile(TileType.BISHOP)
        board[BoardPos("b6").index] = Tile(TileType.BISHOP)
        board[BoardPos("c7").index] = Tile(TileType.BISHOP)
        board[BoardPos("d2").index] = Tile(TileType.ROOK)
        board[BoardPos("d3").index] = Tile(TileType.QUEEN)
        board[BoardPos("d5").index] = Tile(TileType.KEIZAR)
        board[BoardPos("d8").index] = Tile(TileType.KING)
        board[BoardPos("e3").index] = Tile(TileType.KING)
        board[BoardPos("g7").index] = Tile(TileType.QUEEN)
        board[BoardPos("f1").index] = Tile(TileType.KNIGHT)
        for (piece in pieces2) {
            board[piece.pos.value.index].piece = piece
        }
        board
    }

    private val expectations2 = listOf(
        setOf("b1", "b3", "b4", "a2", "c2", "d2"),
        setOf("a4", "a6", "c4", "c6", "d7", "e8"),
        setOf("a5", "a7", "c7", "c5", "d4"),
        setOf("b6", "b8", "d6", "e5", "f4", "g3", "h2"),
        setOf("b2", "c2", "e2", "f2", "g2", "h2", "d1", "d3"),
        setOf(
            "a3", "b3", "c3", "b1", "c2", "d2", "e2", "c4",
            "e4", "f5", "g6", "h7", "d4", "d5", "d6", "d7", "d8"
        ),
        setOf("c8", "d7", "e7", "e8"),
        setOf("d2", "d4", "e2", "e4", "f2", "f3", "f4"),
        setOf(
            "g8", "h7", "h8", "h6", "f8", "d7", "e7", "f7", "f6",
            "e5", "d4", "c3", "b2", "g6", "g5", "g4", "g3", "g2", "g1"
        ),
        setOf("d2", "g3", "h2"),
    ).map { set -> set.map { BoardPos.fromString(it) }.toSet() }

    @TestFactory
    fun `test special piece movements`(): List<DynamicTest> {
        return pieces2.indices.map { i ->
            DynamicTest.dynamicTest("test special piece movement $i") {
                assertEquals(
                    expectations2[i],
                    ruleEngineCore.showValidMoves(board2, pieces2[i]) { index }.toSet()
                )
            }
        }
    }

    @Test
    fun `test movement at Keizar` () = runTest {
        val board = List(64) { Tile(TileType.PLAIN) }.toMutableList()
        board[BoardPos("d5").index] = Tile(TileType.KEIZAR)
        val piece = MutablePiece(0, Role.WHITE, MutableStateFlow(BoardPos("d5")))
        board[BoardPos("d5").index].piece = piece
        val moves = ruleEngineCore.showValidMoves(board, piece) { index }
        assertEquals(0, moves.size)
    }

    @Test
    fun `test black movement on the column next to d`() = runTest {
        val board = List(64) { Tile(TileType.PLAIN) }.toMutableList()
        board[BoardPos("d5").index] = Tile(TileType.KEIZAR)
        val piece = MutablePiece(0, Role.BLACK, MutableStateFlow(BoardPos("c8")))
        board[BoardPos("c8").index].piece = piece
        val moves = ruleEngineCore.showValidMoves(board, piece) { index }
        assertEquals(setOf(BoardPos("c7")), moves.toSet())
    }
}