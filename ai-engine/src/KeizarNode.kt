package org.keizar.aiengine

import kotlinx.coroutines.flow.first
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.game.TileType
import org.keizar.utils.communication.game.BoardPos
import java.util.LinkedList
import java.util.Queue
import kotlin.math.abs

interface TileNode {
    val position: BoardPos
    var occupy: Role?
    var distance: Int
    val parents: MutableList<Pair<TileNode, Int>>
}

open class TileNodeImpl(
    override val position: BoardPos,
    override var occupy: Role?,
    override var distance: Int,
    override val parents: MutableList<Pair<TileNode, Int>>,
) : TileNode

class KeizarNode (
    override var occupy: Role?,
) : TileNodeImpl(
    BoardPos("d5"),
    occupy,
    0,
    mutableListOf(),
)

class DefaultNode(
    override val position: BoardPos,
    override var occupy: Role?,
    override var distance: Int,
    override val parents: MutableList<Pair<TileNode, Int>>,
) : TileNodeImpl(
    position,
    occupy,
    distance,
    parents,
)
class NormalNode(
    override val position: BoardPos,
    override var occupy: Role?,
    override var distance: Int,
    override val parents: MutableList<Pair<TileNode, Int>>,
) : TileNodeImpl(
    position,
    occupy,
    distance,
    parents,
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
            row.add(DefaultNode(BoardPos(i, j), null, Int.MAX_VALUE, mutableListOf()))
        }
        if (i == 0) {
            board[i] = row
        } else {
            board.add(row)
        }
    }
    val occupyKeizar = game.currentRound.first().pieceAt(BoardPos("d5"))
    val startFromKeizar = KeizarNode(occupyKeizar)
    createNode(startFromKeizar, game, board, role)

//    if (role == Role.WHITE) {
//        for (i in 4 until 8) {
//            for (j in 0 until  8) {
//                // if it is opposite plain tile, it can never reach keizar
//                if (tilesArrangement[board[i][j].position] == TileType.PLAIN) {
//                    board[i][j].distance = Int.MAX_VALUE
//                }
//            }
//        }
//    } else {
//        for (i in 0 until 5) {
//            for (j in 0 until  8) {
//                // if it is opposite plain tile, it can never reach keizar
//                if (tilesArrangement[board[i][j].position] == TileType.PLAIN) {
//                    board[i][j].distance = Int.MAX_VALUE
//                }
//            }
//        }
//    }
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
                val occupy = game.currentRound.first().pieceAt(it)
                if (board[it.row][it.col] !is NormalNode) {  // if the node has not been travelled
                    board[it.row][it.col] = NormalNode(it, occupy, node.distance + 1, mutableListOf(Pair(node, node.distance + 1)))
                    queue.add(board[it.row][it.col])
                } else {
                    var checked = false
                    board[it.row][it.col].parents.forEachIndexed { index, parent ->
                        if (parent.first == node) {
                            checked = true
                            board[it.row][it.col].parents[index] = Pair(node, node.distance + 1)
                        }
                    }
                    if (!checked) {
                        board[it.row][it.col].parents.add(Pair(node, node.distance + 1))
                    } else {
                        if (node.distance + 1 < board[it.row][it.col].distance) {
                            board[it.row][it.col].distance = node.distance + 1
                            queue.add(board[it.row][it.col])
                        } else { }
                    }

                }
            }
        }
    }

}

suspend fun getMoves (
    target: BoardPos,
    game: GameSession,
    role:Role,
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
                    val stopped = checkLines(target, j, i, game) || checkDiagonals(target, j, i, game)
                    if (!stopped) {
                        positionList.add(
                            BoardPos(i, j)
                        )
                    }
                }
                TileType.BISHOP -> if (!(target.row == i && target.col == j)
                    && (abs(target.col - j) == abs(target.row - i))) {
                    val stopped = checkDiagonals(target, j, i, game)
                    if (!stopped) {
                        positionList.add(
                            BoardPos(i, j)
                        )
                    }
                }
                TileType.KNIGHT -> if (abs(target.col - j) == 2 && abs(target.row - i) == 1
                    || abs(target.col - j) == 1 && abs(target.row - i) == 2) {
                    positionList.add(
                        BoardPos(i, j)
                    )
                }
                TileType.ROOK -> if (!(target.row == i && target.col == j)
                    && (target.col == j || target.row == i)) {
                    val stopped = checkLines(target, j, i, game)
                    if (!stopped) {
                        positionList.add(
                            BoardPos(i, j)
                        )
                    }
                }
                TileType.KEIZAR -> {}
                TileType.PLAIN -> {
                    if (role == Role.WHITE) {
                        checkForWhitePlain(i, target, j, positionList, game, tileArrangement, role)
                    } else {
                        checkForBlackPlain(i, j, target, positionList, game, tileArrangement, role)
                    }
                }
                null -> {}
            }
        }
    }

    return positionList
}

private suspend fun checkForBlackPlain(
    i: Int,
    j: Int,
    target: BoardPos,
    positionList: MutableList<BoardPos>,
    game: GameSession,
    tileArrangement: Map<BoardPos, TileType>,
    role: Role
) {
    if (i > 5) {
        if (i == 6 && j == 3 || i == 6 && j == 4 || i == 6 && j == 2) {
            if (target.col == j && (target.row - i == -1)) {
                positionList.add(
                    BoardPos(i, j)
                )
            }
        } else {
            checkValidForwardForPlain(target, j, i, positionList, game, tileArrangement, -1)
        }
    } else {
        if (target.col == j && target.row - i == -1) {
            positionList.add(
                BoardPos(i, j)
            )
        }
    }
    checkPlainCapture(game, target, role, i, j, positionList, -1)
}

private suspend fun checkForWhitePlain(
    i: Int,
    target: BoardPos,
    j: Int,
    positionList: MutableList<BoardPos>,
    game: GameSession,
    tileArrangement: Map<BoardPos, TileType>,
    role: Role
) {
    if (i < 2) {
        checkValidForwardForPlain(target, j, i, positionList, game, tileArrangement, 1)
    } else {
        if (target.col == j && target.row - i == 1) {
            positionList.add(
                BoardPos(i, j)
            )
        }
    }
    checkPlainCapture(game, target, role, i, j, positionList, 1)
}
private suspend fun checkValidForwardForPlain(
    target: BoardPos,
    j: Int,
    i: Int,
    positionList: MutableList<BoardPos>,
    game: GameSession,
    tileArrangement: Map<BoardPos, TileType>,
    role: Int
) {
    if (target.col == j && (target.row - i == role || target.row - i == 2 * role)) {
        if (target.row - i == role) {
            positionList.add(
                BoardPos(i, j)
            )
        } else if (target.row - i == 2 * role) {
            if (game.currentRound.first().pieceAt(
                    BoardPos(target.row - role, target.col)
                ) == null && tileArrangement[BoardPos(
                    target.row - role,
                    target.col
                )] == TileType.PLAIN
            ) {
                positionList.add(
                    BoardPos(i, j)
                )
            }
        }
    }
}

private suspend fun checkPlainCapture(
    game: GameSession,
    target: BoardPos,
    role: Role,
    i: Int,
    j: Int,
    positionList: MutableList<BoardPos>,
    offset: Int
) {
    if (game.currentRound.first().pieceAt(target) == role.other()) {
        if (target.row - i == offset && abs(target.col - j) == 1) {
            positionList.add(
                BoardPos(i, j)
            )
        }
    }
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



