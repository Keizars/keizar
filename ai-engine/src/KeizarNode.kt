package org.keizar.aiengine

import org.keizar.game.BoardProperties
import org.keizar.game.Role
import org.keizar.game.TileType
import org.keizar.game.internal.RuleEngineCoreImpl
import org.keizar.game.internal.Tile
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
private val BoardPos.index get() = row * 8 + col

fun createKeizarGraph(
    role: Role,
    tiles: MutableList<Tile>,
):MutableList<MutableList<TileNode>> {
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
    val occupyKeizar = tiles[BoardPos("d5").index].piece?.role
    val startFromKeizar = KeizarNode(occupyKeizar)
    board[4][3] = startFromKeizar
    createNode(startFromKeizar, tiles, board, role)

    return board
}

fun createNode(
    root: TileNode,
    tiles: MutableList<Tile>,
    board: MutableList<MutableList<TileNode>>,
    role: Role
) {
    val ruleEngine = RuleEngineCoreImpl(BoardProperties.getStandardProperties())
    val queue: Queue<TileNode> = LinkedList()
    queue.add(root)
    while (queue.isNotEmpty()) {
        // the parent
        val node = queue.remove()
        val fromTiles = getMoves(node.position, tiles, role)

        if (fromTiles.isNotEmpty()) {
            fromTiles.map {
                val occupy = tiles[it.index].piece?.role
                if (board[it.row][it.col] !is NormalNode) {  // if the node has not been travelled
                    // create a new traversed node at this position, its parent list contains the current node
                    board[it.row][it.col] = NormalNode(it, occupy, node.distance + 1, mutableListOf(Pair(node, node.distance + 1)))
                    queue.add(board[it.row][it.col])
                } else {
                    //the node has been travelled, check if the current node is in the parent list
                    var checked = false
                    board[it.row][it.col].parents.forEachIndexed { index, parent ->
                        // if the parent is in the list, update the distance stored in the parent list and add the current node to the parent list
                        if (parent.first == node) {
                            checked = true
                            board[it.row][it.col].parents[index] = Pair(node, node.distance + 1)
                        }
                    }
                    // the parent is not in the list, add the current node to the parent list
                    if (!checked) {
                        board[it.row][it.col].parents.add(Pair(node, node.distance + 1))
                    } else {
                        // if the current node is in the parent list, check if the parent's distance is larger than the current distance
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

fun getMoves (
    target: BoardPos,
    tiles: MutableList<Tile>,
    role:Role,
): MutableList<BoardPos> {
    val positionList = mutableListOf<BoardPos>()

    for (i in 0 until 8) {
        for (j in 0 until 8) {
            when (tiles[BoardPos(i, j).index].type) {
                TileType.KING -> if (abs(target.col - j) <= 1
                    && abs(target.row - i) <= 1
                    && !(target.row == i && target.col == j)) {
                    positionList.add(
                        BoardPos(i, j)
                    )
                }
                TileType.QUEEN -> if (!(target.row == i && target.col == j)
                    && (target.col == j || target.row == i || abs(target.col - j) == abs(target.row - i))) {
                    val stopped = checkLines(target, j, i, tiles) || checkDiagonals(target, j, i, tiles )
                    if (!stopped) {
                        positionList.add(
                            BoardPos(i, j)
                        )
                    }
                }
                TileType.BISHOP -> if (!(target.row == i && target.col == j)
                    && (abs(target.col - j) == abs(target.row - i))) {
                    val stopped = checkDiagonals(target, j, i, tiles)
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
                    val stopped = checkLines(target, j, i, tiles)
                    if (!stopped) {
                        positionList.add(
                            BoardPos(i, j)
                        )
                    }
                }
                TileType.KEIZAR -> {}
                TileType.PLAIN -> {
                    if (role == Role.WHITE) {
                        checkForWhitePlain(i, target, j, positionList, tiles, role)
                    } else {
                        checkForBlackPlain(i, j, target, positionList, tiles, role)
                    }
                }
                null -> {}
            }
        }
    }

    return positionList
}

private fun checkForBlackPlain(
    i: Int,
    j: Int,
    target: BoardPos,
    positionList: MutableList<BoardPos>,
    tiles: MutableList<Tile>,
    role: Role
) {
    if (i > 5) {
        if (i == 7 && j == 4 || i == 6 && j == 3 || i == 7 && j == 2) {
            if (target.col == j && (target.row - i == -1)) {
                positionList.add(
                    BoardPos(i, j)
                )
            }
        } else {
            checkValidForwardForPlain(target, j, i, positionList, tiles, -1)
        }
    } else {
        if (target.col == j && target.row - i == -1) {
            positionList.add(
                BoardPos(i, j)
            )
        }
    }
    checkPlainCapture(tiles, target, role, i, j, positionList, -1)
}

private fun checkForWhitePlain(
    i: Int,
    target: BoardPos,
    j: Int,
    positionList: MutableList<BoardPos>,
    tiles: MutableList<Tile>,
    role: Role
) {
    if (i < 2) {
        checkValidForwardForPlain(target, j, i, positionList, tiles, 1)
    } else {
        if (target.col == j && target.row - i == 1) {
            positionList.add(
                BoardPos(i, j)
            )
        }
    }
    checkPlainCapture(tiles, target, role, i, j, positionList, 1)
}
private fun checkValidForwardForPlain(
    target: BoardPos,
    j: Int,
    i: Int,
    positionList: MutableList<BoardPos>,
    tiles: MutableList<Tile>,
    role: Int
) {
    if (target.col == j && (target.row - i == role || target.row - i == 2 * role)) {
        if (target.row - i == role) {
            positionList.add(
                BoardPos(i, j)
            )
        } else if (target.row - i == 2 * role) {
            if (tiles[BoardPos(target.row - role, target.col).index].piece?.role
                == null && tiles[BoardPos(
                    target.row - role,
                    target.col
                ).index].type == TileType.PLAIN
            ) {
                positionList.add(
                    BoardPos(i, j)
                )
            }
        }
    }
}

private fun checkPlainCapture(
    tiles: MutableList<Tile>,
    target: BoardPos,
    role: Role,
    i: Int,
    j: Int,
    positionList: MutableList<BoardPos>,
    offset: Int
) {
    if (tiles[target.index].piece?.role == role.other()) {
        if (target.row - i == offset && abs(target.col - j) == 1) {
            positionList.add(
                BoardPos(i, j)
            )
        }
    }
}

private fun checkLines(
    target: BoardPos,
    j: Int,
    i: Int,
    tiles: MutableList<Tile>,
): Boolean {
    var stopped = false
    if (target.col == j) {
        if (target.row > i) {
            for (row in i + 1 until target.row) {
                if (tiles[BoardPos(row, target.col).index].piece != null) {
                    stopped = true
                    break
                }
            }
        } else if (target.row < i) {
            for (row in target.row + 1 until i) {
                if (tiles[BoardPos(row, target.col).index].piece != null) {
                    stopped = true
                    break
                }
            }
        }
    } else if (target.row == i) {
        if (target.col > j) {
            for (col in j + 1 until target.col) {
                if (tiles[BoardPos(target.row, col).index].piece != null) {
                    stopped = true
                    break
                }
            }
        } else if (target.col < j){
            for (col in target.col + 1 until j) {
                if (tiles[BoardPos(target.row, col).index].piece != null) {
                    stopped = true
                    break
                }
            }
        }
    }
    return stopped
}

private fun checkDiagonals(
    target: BoardPos,
    j: Int,
    i: Int,
    tiles: MutableList<Tile>,
): Boolean {
    var stopped = false
    if (target.row > i) {
        if (target.col > j) {
            for (offset in 1 until target.row - i) {
                if (tiles[BoardPos(target.row - offset, target.col - offset).index].piece != null) {
                    stopped = true
                    break
                }
            }
        }
        else if (target.col < j) {
            for (offset in 1 until target.row - i) {
                if (tiles[BoardPos(target.row - offset, target.col + offset).index].piece != null) {
                    stopped = true
                    break
                }
            }
        }
    } else if (target.row < i) {
        if (target.col > j) {
            for (offset in 1 until target.row - i) {
                if (tiles[BoardPos(target.row + offset, target.col - offset).index].piece != null) {
                    stopped = true
                    break
                }
            }
        }
        else if (target.col < j){
            for (offset in 1 until target.row - i) {
                if (tiles[BoardPos(target.row + offset, target.col + offset).index].piece != null) {
                    stopped = true
                    break
                }
            }
        }
    }
    return stopped
}
