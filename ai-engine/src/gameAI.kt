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
import org.keizar.game.Piece
import org.keizar.game.Role
import org.keizar.game.RoundSession
import org.keizar.game.TileType
import org.keizar.game.internal.Tile
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Player
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.random.nextLong

interface GameAI {
    val game: GameSession
    val myPlayer: Player

    fun start()

    suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos>?

    suspend fun end()

}

suspend fun findRandomMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos>? {
    val allPieces = round.getAllPiecesPos(role).first()
    if (allPieces.isEmpty()) {
        return null
    } else {
        var randomPiece = allPieces.random()
        var validTargets = listOf<BoardPos>()

        while (validTargets.isEmpty() && round.winner.first() == null) {
            randomPiece = allPieces.random()
            validTargets = round.getAvailableTargets(randomPiece).first()
        }

        return if (validTargets.isEmpty()) {
            null
        } else {
            val randomTarget = validTargets.random()
            Pair(randomPiece, randomTarget)
        }
    }

}

class RandomGameAIImpl(
    override val game: GameSession,
    override val myPlayer: Player,
    private val parentCoroutineContext: CoroutineContext,
    private val disableDelay: Boolean = false
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
                        val bestPos = findBestMove(session, currentRole)
                        if (!disableDelay) {
                            delay(Random.nextLong(1000L..1500L))
                        }
                        if (bestPos != null) {
                            session.move(bestPos.first, bestPos.second)
                        }
                    }
                }
            }
        }

        myCoroutine.launch {
            game.currentRound.flatMapLatest { it.winner }.collect {
                if (it != null) {
                    game.confirmNextRound(myPlayer)
                }
            }
        }
    }

    override suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos>? {
        val allPieces = round.getAllPiecesPos(role).first()
        var randomPiece = allPieces.random()
        var validTargets = round.getAvailableTargets(randomPiece).first()

        do {
            randomPiece = allPieces.random()
            validTargets = round.getAvailableTargets(randomPiece).first()
            delay(1000L)
        } while (validTargets.isEmpty() && allPieces.isNotEmpty() && round.winner.first() == null)

        return if (validTargets.isEmpty()) {
            null
        } else {
            val randomTarget = validTargets.random()
            Pair(randomPiece, randomTarget)
        }
    }

    override suspend fun end() {
        myCoroutine.cancel()
    }
}

//class QTableAI(
//    override val game: GameSession = GameSession.create(0),
//    override val myPlayer: Player,
//    private val parentCoroutineContext: CoroutineContext,
//    private val test: Boolean = false,
//    private val endpoint: String = "http://home.him188.moe:4393"
//) : GameAI {
//
//    private val client = HttpClient(CIO) {
//        install(ContentNegotiation) {
//            json()
//        }
//        Logging()
//    }
//
//    private val myCoroutine: CoroutineScope =
//        CoroutineScope(parentCoroutineContext + Job(parent = parentCoroutineContext[Job]))
//
//    override fun start() {
//        myCoroutine.launch {
//            game.currentRole(myPlayer).zip(game.currentRound) { myRole, session ->
//                myRole to session
//            }.collectLatest { (myRole, session) ->
//                session.curRole.collect { currentRole ->
//                    if (myRole == currentRole) {
//                        val bestPos = findBestMove(session, currentRole)
//                        if (!test) {
//                            delay(Random.nextLong(1000L..1500L))
//                        }
//                        session.move(bestPos.first, bestPos.second)
//                    }
//                }
//            }
//        }
//
//        myCoroutine.launch {
//            game.currentRound.flatMapLatest { it.winner }.collect {
//                if (it != null) {
//                    game.confirmNextRound(myPlayer)
//                }
//                if (!test) {
//                    delay(Random.nextLong(1000L..1500L))
//                }
//            }
//        }
//    }
//
//    override suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos> {
//        val moves = round.getAllPiecesPos(role).first().flatMap { source ->
//            round.getAvailableTargets(source).first().map { dest ->
//                Move(source, dest, round.pieceAt(dest) != null)
//            }
//        }
//
//        val blackPiece = round.getAllPiecesPos(Role.BLACK).first()
//        val whitePiece = round.getAllPiecesPos(Role.WHITE).first()
//        val seed = game.properties.seed
//        val resp = client.post(
//            endpoint + "/AI/" + if (role == Role.BLACK) "black" else "white"
//        ) {
//            contentType(Application.Json)
//            setBody(buildJsonObject {
//                put("move", buildJsonArray {
//                    for (m in moves) {
//                        add(buildJsonArray {
//                            add(m.source.row)
//                            add(m.source.col)
//                            add(m.dest.row)
//                            add(m.dest.col)
//                            add(m.isCapture)
//                        })
//                    }
//                })
//                put("black_pieces", buildJsonArray {
//                    for (p in blackPiece) {
//                        add(buildJsonArray {
//                            add(p.row)
//                            add(p.col)
//                        })
//                    }
//                })
//                put("white_pieces", buildJsonArray {
//                    for (p in whitePiece) {
//                        add(buildJsonArray {
//                            add(p.row)
//                            add(p.col)
//                        })
//                    }
//                })
//                put("seed", buildJsonArray { add(seed) })
//            })
//        }
//        val move = resp.body<List<Int>>()
//        return BoardPos(move[0], move[1]) to BoardPos(move[2], move[3])
//    }
//
//    override suspend fun end() {
//        myCoroutine.cancel()
//    }
//
//}


class AlgorithmAI(
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
            val rememberStates = mutableListOf<Pair<BoardPos, BoardPos>>()
            game.currentRole(myPlayer).zip(game.currentRound) { myRole, session ->
                myRole to session
            }.collectLatest { (myRole, session) ->
                session.curRole.collect { currentRole ->
                    if (myRole == currentRole) {
                        if (!disableDelay) {
                            delay(Random.nextLong(1000L..1500L))
                        }
                        findBestMove(session, currentRole, rememberStates)
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
        TODO("Not yet implemented")
    }

    private val BoardPos.index get() = row * game.properties.width + col

    private suspend fun initTiles(pieces: List<Piece>): MutableList<Tile> {
        val boardProperties = game.properties
        val tiles = MutableList(boardProperties.width * boardProperties.height) {
            Tile(TileType.PLAIN)
        }
        boardProperties.tileArrangement.toList().map { (pos, type) ->
            tiles[pos.index] = Tile(type)
        }
        for (piece in pieces) {
            if (!piece.isCaptured.first()) {
                val pos = piece.pos.first()
                tiles[pos.index].piece = piece
            }
        }
        return tiles
    }

    private suspend fun findBestMove(
        round: RoundSession,
        role: Role,
        rememberStates: MutableList<Pair<BoardPos, BoardPos>>
    ): Pair<BoardPos, BoardPos>? {
        val tiles = initTiles(round.pieces)
        val board = createKeizarGraph(role, tiles)
        var minDistance = Int.MAX_VALUE
        var moves: MutableList<Pair<BoardPos, BoardPos>> = mutableListOf()
        val keizarMoves: MutableList<Pair<BoardPos, BoardPos>> = mutableListOf()
        var oneStepToKeizarCount = 0
        var oneStepToKeizarCountRole = 0
        val candidateMoves: MutableList<Pair<BoardPos, BoardPos>> = mutableListOf()
        val keizarCapture = game.currentRound.first().pieceAt(game.properties.keizarTilePos)
        val keizarCount = game.currentRound.first().winningCounter.value
        val allowCaptureKeizar =
            (keizarCapture != role && keizarCount > aiParameters.keizarThreshold)
        board.forEach {
            it.forEach { node ->
                if (node.distance == 1 || node.distance == 2) {
                    oneStepToKeizarCount += 1
                }
                if (node is NormalNode && node.occupy == role) {
                    if (node.distance == 1 || node.distance == 2) {
                        oneStepToKeizarCountRole += 1
                    }
                    node.parents.sortBy { parent -> parent.second }
                    for (parent in node.parents) {
                        val notRecOccupyPos =
                            if (role == Role.WHITE) BoardPos("d4") else BoardPos("d6")
                        val notRecOccupy =
                            tiles[notRecOccupyPos.index].type == TileType.BISHOP || tiles[notRecOccupyPos.index].type == TileType.KNIGHT || tiles[notRecOccupyPos.index].type == TileType.PLAIN && keizarCapture != null
                        val recOccupyPos =
                            if (role == Role.WHITE) listOf(BoardPos("c4"), BoardPos("e4")) else listOf(BoardPos("c6"), BoardPos("e6"))
                        val recOccupy = tiles[recOccupyPos[0].index].type == TileType.PLAIN || tiles[recOccupyPos[1].index].type == TileType.PLAIN
                        val checkCapture =
                            tiles[node.position.index].type != TileType.PLAIN ||
                                    ((tiles[node.position.index].type == TileType.PLAIN) && node.position.col != parent.first.position.col)
                        if ((parent.first.position == recOccupyPos[0] || parent.first.position == recOccupyPos[1]) && recOccupy) {
                            if (parent.first.occupy == null || parent.first.occupy == role.other() && checkCapture) {
                                moves.add(node.position to parent.first.position)
                            }
                        } else {
                            if (parent.first.position != notRecOccupyPos || !notRecOccupy) {
                                if (parent.first.occupy == null || parent.first.occupy == role.other() && checkCapture) {
                                    if (parent.second == 1) {
                                        keizarMoves.add(node.position to parent.first.position)
                                    }
                                    if (parent.second in 2..<minDistance && !checkCircle(
                                            node.position to parent.first.position,
                                            rememberStates
                                        )
                                    ) {
                                        minDistance = parent.second
                                        moves = mutableListOf(node.position to parent.first.position)
                                    } else if (parent.second == minDistance && !checkCircle(
                                            node.position to parent.first.position,
                                            rememberStates
                                        )
                                    ) {
                                        moves.add(node.position to parent.first.position)
                                    }
                                    if (parent.second in 2..aiParameters.possibleMovesThreshold && !checkCircle(
                                            node.position to parent.first.position,
                                            rememberStates
                                        )
                                    ) {
                                        candidateMoves.add(node.position to parent.first.position)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        println("Two step to keizar: $oneStepToKeizarCount")
        if (keizarMoves.isNotEmpty() && (allowCaptureKeizar || keizarCapture != role && oneStepToKeizarCountRole >= oneStepToKeizarCount * aiParameters.allowCaptureKeizarThreshold)) {
            println("Keizar Moves: $keizarMoves")
            moves = keizarMoves
        } else {
            val random = Random.nextDouble()
            if (random > aiParameters.noveltyLevel) {
                moves = candidateMoves
//            println("Candidate Moves: $candidateMoves")
            } else {
//            println("Best Moves: $moves")
            }
        }

        if (moves.isNotEmpty()) {
            var bestMove = moves.random()
            var count = 0
            var valid = false
            while (!valid && count < 10) {
                valid = round.move(bestMove.first, bestMove.second)
                if (valid) {
                    rememberStates.add(bestMove)
                }
                bestMove = moves.random()
                count += 1
            }
            if (!valid) {
                val move = findRandomMove(round, role)
                if (move == null) {
                    println("No valid move found")
                } else {
                    round.move(move.first, move.second)
                    rememberStates.add(move)
                }
            }
        } else {
            val move = findRandomMove(round, role)
            if (move == null) {
                println("No valid move found")
            } else {
                round.move(move.first, move.second)
                rememberStates.add(move)
            }
        }
        for (move in rememberStates) {
            println("Remember States: $move")
        }
        return null
    }

    private fun checkCircle(
        move: Pair<BoardPos, BoardPos>,
        rememberState: MutableList<Pair<BoardPos, BoardPos>>
    ): Boolean {
        return if (rememberState.isEmpty()) {
            false
        } else {
            val size = rememberState.size
            val lastMove = rememberState[size - 1]
            val secondLastMove = if (size > 1) rememberState[size - 2] else null
            val reverseMove = move.second to move.first
            assert(move == move.first to move.second)
            if (reverseMove == lastMove) {
                true
            } else move.first == lastMove.second && move.second == secondLastMove?.first && lastMove.first == secondLastMove.second
        }
    }


    override suspend fun end() {
        myCoroutine.cancel()
    }

}

class AIParameters(
    val keizarThreshold: Int = 0,         // Allow capture keizar if keizarCount > keizarThreshold
    val possibleMovesThreshold: Int = 3,    // Collect all possible moves if distance < possibleMovesThreshold for novelty search (or not best move)
    val noveltyLevel: Double = 0.99,
    val allowCaptureKeizarThreshold: Double = 0.3
)    // The probability of not using novelty (or not best move) to enhance exploration of the game tree