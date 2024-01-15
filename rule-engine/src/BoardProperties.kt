package org.keizar.game

import org.keizar.game.local.Piece

class BoardProperties {
    companion object {
        const val BOARD_SIZE = 8
        const val WINNING_COUNT = 3

        val KEIZAR_POS = BoardPos.fromString("d5")
        val STARTING_PLAYER = Piece.Color.WHITE
        val PIECES_STARTING_POS = listOf(
            Piece.Color.WHITE to listOf(
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            ).map { BoardPos.fromString(it) },
            Piece.Color.BLACK to listOf(
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
            ).map { BoardPos.fromString(it) }
        )
        val FIXED_PLAIN_TILES_POS = listOf(
            "a1", "b1", "g1", "h1", "c2", "d2", "e2", "f2",
            "a8", "b8", "g8", "h8", "c7", "d7", "e7", "f7",
        ).map { BoardPos.fromString(it) }
    }
}