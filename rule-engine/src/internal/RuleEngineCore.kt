package org.keizar.game.internal

import org.keizar.game.BoardProperties
import org.keizar.game.Piece
import org.keizar.game.Role
import org.keizar.game.TileType
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.B
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.BL
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.BR
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.F
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.FL
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.FR
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.KBL
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.KBR
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.KFL
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.KFR
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.KLB
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.KLF
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.KRB
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.KRF
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.L
import org.keizar.game.internal.RuleEngineCoreImpl.Route.Direction.R
import kotlin.math.abs
import org.keizar.utils.communication.game.BoardPos

interface RuleEngineCore {
    fun showValidMoves(tiles: List<Tile>, piece: Piece, index: BoardPos.() -> Int): List<BoardPos>
    fun showValidMoves(tiles: List<Tile>, pos: BoardPos, index: BoardPos.() -> Int): List<BoardPos>
    fun showValidSource(
        tiles: List<Tile>,
        pos: BoardPos,
        role: Role,
        index: BoardPos.() -> Int
    ): List<BoardPos>
}

class RuleEngineCoreImpl(
    private val boardProperties: BoardProperties,
) : RuleEngineCore {
    override fun showValidSource(
        tiles: List<Tile>,
        pos: BoardPos,
        role: Role,
        index: BoardPos.() -> Int
    ): List<BoardPos> {
        val validSources = mutableListOf<BoardPos>()

        val reverseSearchRoutes = listOf(
            routesOf(TileType.ROOK, role) to listOf(TileType.ROOK, TileType.QUEEN),
            routesOf(TileType.BISHOP, role) to listOf(TileType.BISHOP, TileType.QUEEN),
            routesOf(TileType.KING, role) to listOf(TileType.KING),
            routesOf(TileType.KNIGHT, role) to listOf(TileType.KNIGHT),
        )

        for ((routes, tileTypes) in reverseSearchRoutes) {
            var curPos = pos
            for (route in routes) {
                for (step in 1..route.reach) {
                    // take one step forward
                    curPos = curPos.step(route.direction)

                    // end this route if out of range
                    if (curPos.outOfRange(boardProperties)) break

                    // end this route if blocked by any piece
                    if (tiles[curPos.index()].piece?.role != null) break

                    // if the current tile is one of the target types, add it to the valid sources
                    if (tiles[curPos.index()].type in tileTypes) {
                        validSources.add(curPos)
                    }
                }
            }
        }

        // special evaluation for pawns: diagonal blocks always seen as valid source, if the
        // tile type is pawn; forward/backward one step only seen as valid source only if there
        // is no piece on the target block.
        val diagonalPos1 = pos.step(if (role == Role.WHITE) BL else FL)
        val diagonalPos2 = pos.step(if (role == Role.WHITE) BR else FR)
        if (tiles[diagonalPos1.index()].type == TileType.PLAIN) validSources.add(diagonalPos1)
        if (tiles[diagonalPos2.index()].type == TileType.PLAIN) validSources.add(diagonalPos2)
        if (tiles[pos.index()].piece == null) {
            val oneStepPos = pos.step(if (role == Role.WHITE) B else F)
            if (tiles[oneStepPos.index()].type == TileType.PLAIN) validSources.add(oneStepPos)
        }

        return validSources
    }

    override fun showValidMoves(
        tiles: List<Tile>,
        piece: Piece,
        index: BoardPos.() -> Int
    ): List<BoardPos> {
        val validMoves = mutableListOf<BoardPos>()

        val curTile = tiles[piece.pos.value.index()]
        val routes = routesOf(curTile.type, piece.role, shouldPawnMoveTwoStep(piece) { pos ->
            tiles[pos.index()].type
        })

        for (route in routes) {
            var curPos = piece.pos.value
            for (step in 1..route.reach) {
                // take one step forward
                curPos = curPos.step(route.direction)

                // end this route if out of range
                if (curPos.outOfRange(boardProperties)) break

                val blockedBy: Role? = tiles[curPos.index()].piece?.role

                // end this route if blocked by an ally piece
                if (blockedBy == piece.role) {
                    break
                } else if (blockedBy == piece.role.other()) {
                    // the route is blocked by an opponent piece
                    // if the route does not allow capturing, end the route
                    if (route.permission == Route.Permission.MOVE) break

                    // otherwise, record it as a valid place and end the route
                    validMoves.add(curPos)
                    break
                } else {
                    // the route is not blocked by anything
                    // end the route if it only allows capturing
                    if (route.permission == Route.Permission.CAPTURE) break

                    // otherwise, record it as a valid place and continue
                    validMoves.add(curPos)
                }
            }
        }

        return validMoves
    }

    override fun showValidMoves(
        tiles: List<Tile>,
        pos: BoardPos,
        index: BoardPos.() -> Int
    ): List<BoardPos> {
        val piece = tiles[pos.index()].piece ?: return listOf()
        return showValidMoves(tiles, piece, index)
    }

    private class Route(
        val direction: Direction,
        val reach: Int = Int.MAX_VALUE,
        val permission: Permission = Permission.BOTH,
    ) {
        enum class Direction {
            // Orthogonal directions
            F, B, L, R,

            // Diagonal directions
            FL, FR, BL, BR,

            // Knight directions
            KFL, KFR, KBL, KBR, KLF, KLB, KRF, KRB;
        }

        enum class Permission {
            MOVE, CAPTURE, BOTH
        }
    }

    private fun BoardPos.step(direction: Route.Direction): BoardPos {
        return when (direction) {
            F -> BoardPos(row + 1, col)
            B -> BoardPos(row - 1, col)
            L -> BoardPos(row, col - 1)
            R -> BoardPos(row, col + 1)
            FL -> BoardPos(row + 1, col - 1)
            FR -> BoardPos(row + 1, col + 1)
            BL -> BoardPos(row - 1, col - 1)
            BR -> BoardPos(row - 1, col + 1)
            KFL -> BoardPos(row + 2, col - 1)
            KFR -> BoardPos(row + 2, col + 1)
            KBL -> BoardPos(row - 2, col - 1)
            KBR -> BoardPos(row - 2, col + 1)
            KLF -> BoardPos(row + 1, col - 2)
            KLB -> BoardPos(row - 1, col - 2)
            KRF -> BoardPos(row + 1, col + 2)
            KRB -> BoardPos(row - 1, col + 2)
        }
    }

    private fun BoardPos.outOfRange(boardProperties: BoardProperties): Boolean {
        return row !in 0..<boardProperties.width || col !in 0..<boardProperties.height
    }

    private fun routesOf(
        tileType: TileType,
        role: Role,
        pawnMovesTwoStep: Boolean = false
    ): List<Route> {
        return when (tileType) {
            TileType.KING -> listOf(
                Route(F, 1), Route(B, 1), Route(L, 1), Route(R, 1),
                Route(FL, 1), Route(FR, 1), Route(BL, 1), Route(BR, 1),
            )

            TileType.QUEEN -> routesOf(TileType.BISHOP, role) + routesOf(TileType.ROOK, role)

            TileType.BISHOP -> listOf(
                Route(FL), Route(FR), Route(BL), Route(BR),
            )

            TileType.ROOK -> listOf(
                Route(F), Route(B), Route(L), Route(R),
            )

            TileType.KNIGHT -> listOf(
                Route(KFL, 1), Route(KFR, 1), Route(KBL, 1), Route(KBR, 1),
                Route(KLF, 1), Route(KLB, 1), Route(KRF, 1), Route(KRB, 1),
            )

            TileType.KEIZAR -> listOf()

            TileType.PLAIN -> listOf(
                Route(
                    if (role == Role.WHITE) F else B,
                    if (pawnMovesTwoStep) 2 else 1,
                    Route.Permission.MOVE
                ),
                Route(if (role == Role.WHITE) FL else BL, 1, Route.Permission.CAPTURE),
                Route(if (role == Role.WHITE) FR else BR, 1, Route.Permission.CAPTURE),
            )
        }
    }

    private fun shouldPawnMoveTwoStep(piece: Piece, tileAt: (BoardPos) -> TileType): Boolean {
        // If the piece is not in one of its starting positions, it can't move 2 steps
        if (boardProperties.piecesStartingPos[piece.role]?.contains(piece.pos.value) != true) {
            return false
        }

        // If it is a black piece in the KEIZAR column, it can't move 2 steps
        if (piece.role == Role.BLACK && piece.pos.value.col == boardProperties.keizarTilePos.col) {
            return false
        }

        // If it is a black piece that is the first 2 piece on a column next to the KEIZAR column,
        // it can't move 2 steps
        if (piece.role == Role.BLACK && abs(piece.pos.value.col - boardProperties.keizarTilePos.col)
            == 1 && piece.pos.value.row == 7
        ) {
            return false
        }

        // If the tile right in front of the piece is not a PLAIN tile, it can't move 2 steps
        // Otherwise, it can move 2 steps
        return tileAt(
            BoardPos(
                piece.pos.value.row + if (piece.role == Role.WHITE) 1 else -1,
                piece.pos.value.col,
            )
        ) == TileType.PLAIN
    }
}
