package org.keizar.aiengine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.game.RoundSession
import org.keizar.game.TileType
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Player
import java.util.LinkedList
import java.util.Queue
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.random.Random
import kotlin.random.nextLong

//Todo： This file cannot run due to circular dependency on the distance property of the rTileNode interface

// defined tile node interface for the graph
// position: the position of the node
// occupy: the player that occupies the node, null if no player occupies the node
// distance: the distance from the Keizar node
// parents: the parent nodes that can move to the current node
interface rTileNode {
    val position: BoardPos
    var occupy: Role?
    val distance: Int
        get() = if (parents.isEmpty()) 1000 else parents.minOf { it.distance } + 1
    val parents: MutableList<rTileNode>
}


class rKeizarNode (
    override val position: BoardPos = BoardPos("d5"),
    override var occupy: Role?,
    override val distance: Int = 0,
    override val parents: MutableList<rTileNode> = mutableListOf()
) : rTileNode

class rDefaultNode(
    override val position: BoardPos,
    override var occupy: Role?,
    override val parents: MutableList<rTileNode>,
) : rTileNode

class TraversedNode(
    override val position: BoardPos,
    override var occupy: Role?,
    override val parents: MutableList<rTileNode>,
) : rTileNode

suspend fun rcreateKeizarGraph(
    role: Role,
    game: GameSession = GameSession.create(0),
): MutableList<MutableList<rTileNode>> {
    val tilesArrangement = game.properties.tileArrangement
    val board = mutableListOf(mutableListOf<rTileNode>())
    for (i in 0 until 8) {
        val row = mutableListOf<rTileNode>()
        for (j in 0 until 8) {
            row.add(rDefaultNode(BoardPos(i, j), null, mutableListOf()))
        }
        if (i == 0) {
            board[i] = row
        } else {
            board.add(row)
        }
    }
    val occupyKeizar = game.currentRound.first().pieceAt(BoardPos("d5"))
    val startFromKeizar = rKeizarNode(occupy = occupyKeizar)

    return rcreateNode(startFromKeizar, game, board, role)
}

suspend fun rcreateNode(
    root: rTileNode,
    game: GameSession,
    board: MutableList<MutableList<rTileNode>>,
    role: Role
): MutableList<MutableList<rTileNode>> {
    // BFS to create connection in the graph
    val queue: Queue<rTileNode> = LinkedList()
    queue.add(root)
    while (queue.isNotEmpty()) {
        val node = queue.remove()
        // get the possible moves from the current node
        val fromTiles = rgetMoves(node.position, game, role)

        if (fromTiles.size > 0) {
            fromTiles.map { pos ->
                val occupy = game.currentRound.first().pieceAt(pos)
                if (board[pos.row][pos.col] !is TraversedNode) {  // if the node has not been travelled, change it to a traversed node
                    board[pos.row][pos.col] = TraversedNode(pos, occupy, mutableListOf(node))
                    queue.add(board[pos.row][pos.col])
                } else {
                    if (!board[pos.row][pos.col].parents.contains(node)) {
                        board[pos.row][pos.col].parents.add(node)
                    } else {}
                }
            }
        }
    }
    return board

}

suspend fun rgetMoves (
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
        if (i == 6 && j == 3 || i == 6 && j == 4 || i == 6 && j == 2 || i == 7 && j == 3) {
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
            for (col in j + 1 until target.col) {
                if (game.currentRound.first().pieceAt(BoardPos(target.row, col)) != null) {
                    stopped = true
                    break
                }
            }
        } else if (target.col < j){
            for (col in target.col + 1 until j) {
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

class rAlgorithmAI(
    override val game: GameSession = GameSession.create(0),
    override val myPlayer: Player,
    private val parentCoroutineContext: CoroutineContext,
    private val disableDelay: Boolean = false,
//    private val endpoint: String = "http://home.him188.moe:4393",
//    private val moves: MutableList<Pair<BoardPos, BoardPos>> = mutableListOf(),
//    private val kei_numbers: MutableList<Int> = mutableListOf(),
    private val aiParameters: AIParameters = AIParameters(),
    private val test: Boolean = false
) : GameAI {
    private val myCoroutine: CoroutineScope =
        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))
    override fun start() {
        myCoroutine.launch {
            game.currentRole(myPlayer).zip(game.currentRound) { myRole, session ->
                myRole to session
            }.collectLatest { (myRole, session) ->
                session.curRole.collect { currentRole ->
                    if (myRole == currentRole) {
                        if (!disableDelay) {
                            delay(Random.nextLong(1000L..1500L))
                        }
                        findBestMove(session, currentRole)
                    }
                }
            }
        }

        myCoroutine.launch {
            game.currentRound.flatMapLatest { it.winner }.collect {
                if (it != null) {
                    game.confirmNextRound(myPlayer)
                }
                if (!disableDelay) {
                    delay(Random.nextLong(1000L..1500L))
                }
            }
        }

    }

    override suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos>? {
        val tiles = game.properties.tileArrangement
        val board = rcreateKeizarGraph(role, game)
        var minDistance = Int.MAX_VALUE
        var moves: MutableList<Pair<BoardPos, BoardPos>> = mutableListOf()
        val candidateMoves: MutableList<Pair<BoardPos, BoardPos>> = mutableListOf()
        val keizarCapture = game.currentRound.first().pieceAt(game.properties.keizarTilePos)
        val keizarCount = game.currentRound.first().winningCounter.value
        val allowCaptureKeizar =
            (keizarCapture != role && keizarCount > aiParameters.keizarThreshold) // TODO: Change keizarCount to a better value
        board.forEach {
            it.forEach {node ->
                if (node is TraversedNode && node.occupy == role) {
                    node.parents.sortBy { parent -> parent.distance + 1 }
                    for (parent in node.parents ) {
                        // the piece in front of the keizar tile
                        val notRecOccupyPos = if (role == Role.WHITE) BoardPos("d4") else BoardPos("d6")
                        // check if the piece in front of the keizar tile is a special piece that allows us to move to the keizar tile
                        val notRecOccupy = tiles[notRecOccupyPos] == TileType.ROOK || tiles[notRecOccupyPos] == TileType.QUEEN || tiles[notRecOccupyPos] == TileType.KING
                        val checkValid = tiles[node.position] != TileType.PLAIN
                                || ((tiles[node.position] == TileType.PLAIN) && node.position.col != parent.position.col)
                        if (parent.position != notRecOccupyPos || notRecOccupy) {
                            if (parent.occupy == null || parent.occupy == role.other() && checkValid) {
                                val lowerBound = if (allowCaptureKeizar) 1 else 2
                                if (parent.distance + 1 in lowerBound..< minDistance) {
                                    minDistance = parent.distance + 1
                                    moves = mutableListOf(node.position to parent.position)
                                } else if (parent.distance + 1 == minDistance) {
                                    moves.add(node.position to parent.position)
                                }
                                if (parent.distance + 1 in lowerBound..aiParameters.possibleMovesThreshold) {
                                    candidateMoves.add(node.position to parent.position)
                                }
                            }
                        }

                    }
                }
            }
        }
        val random = Random.nextDouble()
        if (random > aiParameters.noveltyLevel) {
            moves = candidateMoves
//            println("Candidate Moves: $candidateMoves")
        } else {
//            println("Best Moves: $moves")
        }
        if (moves.isNotEmpty()) {
            var bestMove = moves.random()
            var count = 0
            var valid = false
            while (!valid && count < 10) {
                valid = round.move(bestMove.first, bestMove.second)
                bestMove = moves.random()
                count += 1
            }
            if (!valid) {
                val move = findRandomMove(round, role)
                if (move == null) {
                    println("No valid move found")
                } else {
                    round.move(move.first, move.second)
                }
            }
        } else {
            val move = findRandomMove(round, role)
            if (move == null) {
                println("No valid move found")
            } else {
                round.move(move.first, move.second)
            }
        }
        return null
    }


    override suspend fun end() {
        myCoroutine.cancel()
    }

}



