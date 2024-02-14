package org.keizar.aiengine

import kotlinx.coroutines.flow.first
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.game.TileType
import org.keizar.utils.communication.game.BoardPos
import java.util.LinkedList
import java.util.Queue
import kotlin.math.abs

enum class PieceType {
    WHITE,
    BLACK,
    EMPTY
}
interface TileNode {
    val position: BoardPos
    var occupy: PieceType
    var distance: Int
    val parents: MutableList<TileNode>
    var chosenParent: TileNode?
}

open class TileNodeImpl(
    override val position: BoardPos,
    override var occupy: PieceType,
    override var distance: Int,
    override val parents: MutableList<TileNode>,
    override var chosenParent: TileNode?
) : TileNode

class KeizarNode (
    override var occupy: PieceType,
) : TileNodeImpl(
    BoardPos("d5"),
    occupy,
    0,
    mutableListOf(),
    null
)

class DefaultNode(
    override val position: BoardPos,
    override var occupy: PieceType,
    override var distance: Int,
    override val parents: MutableList<TileNode>,
    override var chosenParent: TileNode?
) : TileNodeImpl(
    position,
    occupy,
    distance,
    parents,
    null
)
class NormalNode(
    override val position: BoardPos,
    override var occupy: PieceType,
    override var distance: Int,
    override val parents: MutableList<TileNode>,
    override var chosenParent: TileNode?
) : TileNodeImpl(
    position,
    occupy,
    distance,
    parents,
    null
)

suspend fun createKeizarGraph(
    role: Role,
    game: GameSession = GameSession.create(0),
):MutableList<MutableList<TileNode>> {
    val tilesArrangement = game.properties.tileArrangement
    val board = mutableListOf(mutableListOf<TileNode>())
    for (i in 0 until 8) {
        val row = mutableListOf<TileNode>()
        for (j in 0 until 8) {
            val occupy = when(game.currentRound.first().pieceAt(BoardPos("d5"))) {
                Role.WHITE -> PieceType.WHITE
                Role.BLACK -> PieceType.BLACK
                null -> PieceType.EMPTY
            }
            row.add(DefaultNode(BoardPos(i, j), PieceType.EMPTY, Int.MAX_VALUE, mutableListOf(), null))
        }
        if (i == 0) {
            board[i] = row
        } else {
            board.add(row)
        }
    }
    val occupy = when(game.currentRound.first().pieceAt(BoardPos("d5"))) {
        Role.WHITE -> PieceType.WHITE
        Role.BLACK -> PieceType.BLACK
        null -> PieceType.EMPTY
    }
    val startFromKeizar = KeizarNode(occupy)
    createNode(startFromKeizar, game, board, role)

    if (role == Role.WHITE) {
        for (i in 4 until 8) {
            for (j in 0 until  8) {
                // if it is opposite plain tile, it can never reach keizar
                if (tilesArrangement[board[i][j].position] == TileType.PLAIN) {
                    board[i][j].distance = Int.MAX_VALUE
                }
            }
        }
    } else {
        for (i in 0 until 5) {
            for (j in 0 until  8) {
                // if it is opposite plain tile, it can never reach keizar
                if (tilesArrangement[board[i][j].position] == TileType.PLAIN) {
                    board[i][j].distance = Int.MAX_VALUE
                }
            }
        }
    }
    return board
}

suspend fun createNode(
    root: TileNode,
    game: GameSession,
    board: MutableList<MutableList<TileNode>>,
    role: Role
) {
    val queue: Queue<TileNode> = LinkedList()
    queue.add(root)
    while (queue.isNotEmpty()) {
        val node = queue.remove()
        val fromTiles = getMoves(node.position, game, role)

        if (fromTiles.size > 0) {
            fromTiles.map {
                val occupy = when(game.currentRound.first().pieceAt(BoardPos("d5"))) {
                    Role.WHITE -> PieceType.WHITE
                    Role.BLACK -> PieceType.BLACK
                    null -> PieceType.EMPTY
                }
                if (board[it.row][it.col] !is NormalNode) {  // if the node has not been travelled
                    board[it.row][it.col] = NormalNode(it, occupy, node.distance + 1, mutableListOf(node), node)
                    queue.add(board[it.row][it.col])
                } else {
                    if (node.distance + 1 >= board[it.row][it.col].distance)
                        board[it.row][it.col].parents.add(node)
                    else {
                        board[it.row][it.col].parents.add(node)
                        board[it.row][it.col].distance = node.distance + 1
                        board[it.row][it.col].chosenParent = node
                        queue.add(root)
                    }
                }
            }
        }
    }

}

suspend fun getMoves (
    target: BoardPos,
    game: GameSession,
    role:Role
): MutableList<BoardPos> {
    val positionList = mutableListOf<BoardPos>()
    val tileArrangement = game.properties.tileArrangement

    for (i in 0 until 8) {
        for (j in 0 until 8) {
            when (game.properties.tileArrangement[BoardPos(i, j)]) {
                TileType.KING -> if (abs(target.col - j) <= 1
                    && abs(target.row - i) <= 1
                    && !(target.row == i && target.col == j)) {
                    positionList.add(
                        BoardPos(i, j)
                    )
                }
                TileType.QUEEN -> if (!(target.row == i && target.col == j)
                    && (target.col == j || target.row == i || abs(target.col - j) == abs(target.row - i))) {
                    if (game.currentRound.first().pieceAt(target) != role) {
                        val stopped = checkLines(target, j, i, game) || checkDiagonals(target, j, i, game)
                        if (!stopped) {
                            positionList.add(
                                BoardPos(i, j)
                            )
                        }
                    }
                }
                TileType.BISHOP -> if (!(target.row == i && target.col == j)
                    && (abs(target.col - j) == abs(target.row - i))) {
                    if (game.currentRound.first().pieceAt(target) != role) {
                        val stopped = checkDiagonals(target, j, i, game)
                        if (!stopped) {
                            positionList.add(
                                BoardPos(i, j)
                            )
                        }
                    }
                }
                TileType.KNIGHT -> if (abs(target.col - j) == 2 && abs(target.row - i) == 1
                    || abs(target.col - j) == 1 && abs(target.row - i) == 2) {
                    if (game.currentRound.first().pieceAt(target) != role || target == game.properties.keizarTilePos) {
                        positionList.add(
                            BoardPos(i, j)
                        )
                    }
                }
                TileType.ROOK -> if (!(target.row == i && target.col == j)
                    && (target.col == j || target.row == i)) {
                    if (game.currentRound.first().pieceAt(target) != role || target == game.properties.keizarTilePos) {
                        val stopped = checkLines(target, j, i, game)
                        if (!stopped) {
                            positionList.add(
                                BoardPos(i, j)
                            )
                        }
                    }
                }
                TileType.KEIZAR -> {}
                TileType.PLAIN -> {
                    if (role == Role.WHITE) {
                        if (i < 2) {
                            if (target.col == j && (target.row - i == 1 || target.row - i == 2)) {
                                if (target.row - i == 1) {
                                    if (game.currentRound.first().pieceAt(target) == null) {
                                        positionList.add(
                                            BoardPos(i, j)
                                        )
                                    }
                                } else if (target.row - i == 2) {
                                    if (game.currentRound.first().pieceAt(target) == null && game.currentRound.first().pieceAt(
                                            BoardPos(target.row - 1, target.col)
                                        ) == null && tileArrangement[BoardPos(target.row - 1, target.col)] == TileType.PLAIN) {
                                        positionList.add(
                                            BoardPos(i, j)
                                        )
                                    }
                                }
                            }
                        } else {
                            if (target.col == j && target.row - i == 1) {
                                if (game.currentRound.first().pieceAt(target) == null) {
                                    positionList.add(
                                        BoardPos(i, j)
                                    )
                                }
                            }
                        }
                        if (game.currentRound.first().pieceAt(target) == Role.BLACK) {
                            if (target.row - i == 1 && abs(target.col - j) == 1) {
                                positionList.add(
                                    BoardPos(i, j)
                                )
                            }
                        }
                    } else {
                        if (i > 5) {
                            if (i == 6 && j == 3 || i == 6 && j == 4 || i == 6 && j == 2) {
                                if (target.col == j && (target.row - i == -1)) {
                                    if (game.currentRound.first().pieceAt(target) == null) {
                                        positionList.add(
                                            BoardPos(i, j)
                                        )
                                    }
                                }
                            } else {
                                if (target.col == j && (target.row - i == -1 || target.row - i == -2)) {
                                    if (target.row - i == -1) {
                                        if (game.currentRound.first().pieceAt(target) == null) {
                                            positionList.add(
                                                BoardPos(i, j)
                                            )
                                        }
                                    } else if (target.row - i == -2) {
                                        if (game.currentRound.first().pieceAt(target) == null && game.currentRound.first().pieceAt(
                                                BoardPos(target.row + 1, target.col)
                                            ) == null  && tileArrangement[BoardPos(target.row + 1, target.col)] == TileType.PLAIN) {
                                            positionList.add(
                                                BoardPos(i, j)
                                            )
                                        }
                                    }
                                }
                            }

                        } else {
                            if (target.col == j && target.row - i == -1) {
                                if (game.currentRound.first().pieceAt(target) == null) {
                                    positionList.add(
                                        BoardPos(i, j)
                                    )
                                }
                            }
                        }
                        if (game.currentRound.first().pieceAt(target) == Role.WHITE) {
                            if (target.row - i == -1 && abs(target.col - j) == 1) {
                                positionList.add(
                                    BoardPos(i, j)
                                )
                            }
                        }
                    }
                }
                null -> {}
            }
        }
    }

    return positionList
}

private suspend fun checkLines(
    target: BoardPos,
    j: Int,
    i: Int,
    game: GameSession
): Boolean {
    var stopped = false
    if (target.col == j) {
        if (target.row > i) {
            for (row in i + 1 until target.row) {
                if (game.currentRound.first().pieceAt(BoardPos(row, target.col)) != null) {
                    stopped = true
                    break
                }
            }
        } else if (target.row < i) {
            for (row in target.row + 1 until i) {
                if (game.currentRound.first().pieceAt(BoardPos(row, target.col)) != null) {
                    stopped = true
                    break
                }
            }
        }
    } else if (target.row == i) {
        if (target.col > j) {
            for (col in j + 1 until target.row) {
                if (game.currentRound.first().pieceAt(BoardPos(target.row, col)) != null) {
                    stopped = true
                    break
                }
            }
        } else if (target.col < j){
            for (col in target.row + 1 until j) {
                if (game.currentRound.first().pieceAt(BoardPos(target.row, col)) != null) {
                    stopped = true
                    break
                }
            }
        }
    }
    return stopped
}

private suspend fun checkDiagonals(
    target: BoardPos,
    j: Int,
    i: Int,
    game: GameSession
): Boolean {
    var stopped = false
    if (target.row > i) {
        if (target.col > j) {
            for (offset in 1 until target.row - i) {
                if (game.currentRound.first().pieceAt(BoardPos(target.row - offset, target.col - offset)) != null) {
                    stopped = true
                    break
                }
            }
        }
        else if (target.col < j) {
            for (offset in 1 until target.row - i) {
                if (game.currentRound.first().pieceAt(BoardPos(target.row - offset, target.col + offset)) != null) {
                    stopped = true
                    break
                }
            }
        }
    } else if (target.row < i) {
        if (target.col > j) {
            for (offset in 1 until target.row - i) {
                if (game.currentRound.first().pieceAt(BoardPos(target.row + offset, target.col - offset)) != null) {
                    stopped = true
                    break
                }
            }
        }
        else if (target.col < j){
            for (offset in 1 until target.row - i) {
                if (game.currentRound.first().pieceAt(BoardPos(target.row + offset, target.col + offset)) != null) {
                    stopped = true
                    break
                }
            }
        }
    }
    return stopped
}



