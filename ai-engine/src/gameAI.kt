package org.keizar.aiengine


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import org.keizar.game.GameSession
import org.keizar.game.MutablePiece
import org.keizar.game.Role
import org.keizar.game.RoundSession
import org.keizar.game.TileType
import org.keizar.game.asPiece
import org.keizar.game.internal.RuleEngineCore
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
        val board = createKeizarGraph(role, game)
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
            it.forEach {node ->
                if (node.distance == 1 || node.distance == 2) {
                    oneStepToKeizarCount += 1
                }
                if (node is NormalNode && node.occupy == role) {
                    if (node.distance == 1 || node.distance == 2) {
                        oneStepToKeizarCountRole += 1
                    }
                    node.parents.sortBy { parent -> parent.second }
                    for (parent in node.parents ) {
//                        val notRecOccupyPos = if (role == Role.WHITE) BoardPos("d4") else BoardPos("d6")
//                        val notRecOccupy = tiles[notRecOccupyPos] == TileType.ROOK || tiles[notRecOccupyPos] == TileType.QUEEN || tiles[notRecOccupyPos] == TileType.KING
                        val checkCapture = tiles[node.position] != TileType.PLAIN
                                || ((tiles[node.position] == TileType.PLAIN) && node.position.col != parent.first.position.col)
//                        if (parent.first.position != notRecOccupyPos || notRecOccupy) {
                            if (parent.first.occupy == null || parent.first.occupy == role.other() && checkCapture) {
                                if (parent.second == 1) {
                                    keizarMoves.add(node.position to parent.first.position)
                                }
                                if (parent.second in 2..< minDistance) {
                                    minDistance = parent.second
                                    moves = mutableListOf(node.position to parent.first.position)
                                } else if (parent.second == minDistance) {
                                    moves.add(node.position to parent.first.position)
                                }
                                if (parent.second in 2..aiParameters.possibleMovesThreshold) {
                                    candidateMoves.add(node.position to parent.first.position)
                                }
                            }
//                        }
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

class AIParameters(
    val keizarThreshold: Int = 0,         // Allow capture keizar if keizarCount > keizarThreshold
    val possibleMovesThreshold: Int = 3,    // Collect all possible moves if distance < possibleMovesThreshold for novelty search (or not best move)
    val noveltyLevel: Double = 0.99,
    val allowCaptureKeizarThreshold: Double = 0.5)    // The probability of not using novelty (or not best move) to enhance exploration of the game tree


class ScoringAlgorithmAI(
    override val game: GameSession = GameSession.create(0),
    override val myPlayer: Player,
    private val parentCoroutineContext: CoroutineContext,
    private val disableDelay: Boolean = false,
//    private val endpoint: String = "http://home.him188.moe:4393",
//    private val moves: MutableList<Pair<BoardPos, BoardPos>> = mutableListOf(),
//    private val kei_numbers: MutableList<Int> = mutableListOf(),
//    private val aiParameters: AIParameters = AIParameters(),
    private val test: Boolean = false,
    private val ruleEngine: RuleEngineCore
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
                if (!disableDelay) {
                    delay(Random.nextLong(1000L..1500L))
                }
            }
        }

    }

    private val BoardPos.index get() = row * game.properties.width + col

    private fun initTiles(): MutableList<Tile> {
        val boardProperties = game.properties
        val tiles = MutableList(boardProperties.width * boardProperties.height) {
            Tile(TileType.PLAIN)
        }
        boardProperties.tileArrangement.toList().map { (pos, type) ->
            tiles[pos.index] = Tile(type)
        }
        var index = 0
        for ((color, startingPos) in boardProperties.piecesStartingPos) {
            for (pos in startingPos) {
                val piece = MutablePiece(index++, color, MutableStateFlow(pos))
                tiles[pos.index].piece = piece.asPiece()
            }
        }
        return tiles
    }

    override suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos>? {
        // tile arrangement
        val tileArrangement = game.properties.tileArrangement
        // Initialize a internal game board for AI to maintain
        val tiles = initTiles()

        val board = createKeizarGraph(role, game)
        board.forEach{
            it.forEach {node ->
                println(node.distance)
            }
        }
        val selfPieces = round.getAllPiecesPos(role).first()
        val opponentPieces = round.getAllPiecesPos(role.other()).first()
        var highestScore = Int.MIN_VALUE
        val keizarCount = round.winningCounter.first()
        var chosenMove: Pair<BoardPos, BoardPos>? = null
        selfPieces.forEach { pos ->
            // get the valid targets of the piece at pos
            val targets = ruleEngine.showValidMoves(tiles, pos) { index }

            targets.forEach { target ->
                val newSelfPieces = selfPieces.toMutableList()
                val newOpponentPieces = opponentPieces.toMutableList()
                updateNewPieces(newSelfPieces, newOpponentPieces, pos, target)

                updateInternalMove(tiles, pos, target)

                val selfScore = newSelfPieces.sumOf { pos -> score(pos, tileArrangement, board) }
                val opponentScore = newOpponentPieces.sumOf { pos -> score(pos, tileArrangement, board) }
                val newScore = selfScore - opponentScore
                if (newScore > highestScore) {
                    highestScore = newScore
                    chosenMove = pos to target
                }
            }
        }
        if (chosenMove == null) {
            println("No valid move found")
        }
        return chosenMove
    }

    override suspend fun end() {
        myCoroutine.cancel()
    }

    private fun updateInternalMove(tiles: MutableList<Tile>, source: BoardPos, target: BoardPos) {
        val sourceIndex = source.index
        val targetIndex = target.index
        tiles[targetIndex].piece = tiles[sourceIndex].piece
        tiles[sourceIndex].piece = null
    }

    private fun updateNewPieces(selfPieces: MutableList<BoardPos>, opponentPieces: MutableList<BoardPos>, source: BoardPos, target: BoardPos) {
        selfPieces.remove(source)
        selfPieces.add(target)
        if (target in opponentPieces) {
            opponentPieces.remove(target)
        }
    }

    private fun undoNewPieces(selfPieces: MutableList<BoardPos>, opponentPieces: MutableList<BoardPos>, source: BoardPos, target: BoardPos) {
        selfPieces.remove(target)
        selfPieces.add(source)
        if (target in opponentPieces) {
            opponentPieces.add(target)
        }
    }

    private fun score(pos: BoardPos, tiles: Map<BoardPos, TileType>, board: MutableList<MutableList<TileNode>>): Int {
        val node = board[pos.row][pos.col]
//        return if (node.distance == Int.MAX_VALUE && tiles[pos] != TileType.KEIZAR) {
//            0
//        } else {
////            if (node.distance <= 3) {
////                val value = when (tiles[pos]) {
////                    TileType.ROOK -> 10
////                    TileType.QUEEN -> 12    // Queen is more valuable than other pieces
////                    TileType.KING -> 10
////                    TileType.BISHOP -> 10
////                    TileType.KNIGHT -> 10
////                    TileType.PLAIN -> 8     // Plain is less valuable than other pieces
////                    TileType.KEIZAR -> Int.MAX_VALUE   // Keizar is the most valuable piece
////                    else -> 0
////                }
////                if (tiles[pos] == TileType.KEIZAR) {
////                    return value
////                } else {
////                    return value - node.distance
////                }
////            } else {
////                return 0
////            }
//            return when(node.distance) {
//                1 -> 10
//                2 -> 8
//                3 -> 6
//                else -> 0
//            }
//        }
        return when(node.distance) {
            1 -> {
                if (tiles[pos] == TileType.KEIZAR) {
                    20
                } else {
                    10
                }
            }
            2 -> 8
            3 -> 6
            else -> 0
        }
    }

}