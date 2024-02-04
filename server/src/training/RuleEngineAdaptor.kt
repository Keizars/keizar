package org.keizar.server.training

import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.Role
import org.keizar.game.TileType
import org.keizar.game.internal.Board
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.serialization.PieceSnapshot
import org.keizar.utils.communication.game.BoardPos

/***
 * A class object highly coupled with keizar.game.internal.Board used for AI training
 */
object RuleEngineAdaptor {
    fun getPossibleMoves(
        properties: BoardProperties,
        boardInput: List<List<Int>>,
        role: Role
    ): List<Move> {
        val board = Board(
            boardProperties = properties,
            ruleEngineCore = RuleEngineCoreImpl(properties),
        )
        board.rearrangePieces(parseBoardInput(boardInput))
        return board.getAllPiecesPos(role).flatMap { source ->
            board.showValidMoves(board.pieceAt(source)!!).map { dest ->
                Move(source, dest, board.pieceAt(dest) != null)
            }
        }
    }

    private fun parseBoardInput(boardInput: List<List<Int>>): List<PieceSnapshot> {
        var index = 0
        return boardInput.flatMapIndexed { rowIndex, row ->
            row.mapIndexedNotNull { colIndex, piece ->
                when {
                    piece == 0 -> null

                    piece > 0 -> PieceSnapshot(
                        index = index++,
                        role = Role.WHITE,
                        pos = BoardPos(rowIndex, colIndex),
                        isCaptured = false
                    )

                    else -> PieceSnapshot(
                        index = index++,
                        role = Role.BLACK,
                        pos = BoardPos(rowIndex, colIndex),
                        isCaptured = false
                    )
                }
            }
        }
    }

    fun encodeBoard(boardProperties: BoardProperties): List<List<Int>> {
        return (0..<boardProperties.width).map { row ->
            (0..<boardProperties.height).map { col ->
                boardProperties.tileArrangement[BoardPos(row, col)].let {
                    when (it) {
                        TileType.PLAIN -> 0
                        TileType.KING -> 1
                        TileType.QUEEN -> 2
                        TileType.BISHOP -> 4
                        TileType.KNIGHT -> 5
                        TileType.ROOK -> 3
                        TileType.KEIZAR -> 7
                        else -> error("Unexpected TileType")
                    }
                }
            }
        }
    }
}