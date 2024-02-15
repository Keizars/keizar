package org.keizar.aiengine


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.keizar.game.GameSession
import org.keizar.game.Move
import org.keizar.game.Role
import org.keizar.game.RoundSession
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

suspend fun findRandomMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos> {
    val allPieces = round.getAllPiecesPos(role).first()
    var randomPiece = allPieces.random()
    var validTargets = round.getAvailableTargets(randomPiece).first()

    do {
        randomPiece = allPieces.random()
        validTargets = round.getAvailableTargets(randomPiece).first()
        delay(1000L)
    } while (validTargets.isEmpty() && allPieces.isNotEmpty() && round.winner.first() == null)

    val randomTarget = validTargets.random()
    return Pair(randomPiece, randomTarget)
}

class RandomGameAIImpl(
    override val game: GameSession,
    override val myPlayer: Player,
    private val parentCoroutineContext: CoroutineContext,
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
                        val bestPos = findBestMove(session, currentRole)
                        if (!test) {
                            delay(Random.nextLong(1000L..1500L))
                        }
                        session.move(bestPos.first, bestPos.second)
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

    override suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos> {
        val allPieces = round.getAllPiecesPos(role).first()
        var randomPiece = allPieces.random()
        var validTargets = round.getAvailableTargets(randomPiece).first()

        do {
            randomPiece = allPieces.random()
            validTargets = round.getAvailableTargets(randomPiece).first()
            delay(1000L)
        } while (validTargets.isEmpty() && allPieces.isNotEmpty() && round.winner.first() == null)

        val randomTarget = validTargets.random()
        return Pair(randomPiece, randomTarget)
    }

    override suspend fun end() {
        myCoroutine.cancel()
    }
}

class QTableAI(
    override val game: GameSession = GameSession.create(0),
    override val myPlayer: Player,
    private val parentCoroutineContext: CoroutineContext,
    private val test: Boolean = false,
    private val endpoint: String = "http://home.him188.moe:4393"
) : GameAI {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        Logging()
    }

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
                        if (!test) {
                            delay(Random.nextLong(1000L..1500L))
                        }
                        session.move(bestPos.first, bestPos.second)
                    }
                }
            }
        }

        myCoroutine.launch {
            game.currentRound.flatMapLatest { it.winner }.collect {
                if (it != null) {
                    game.confirmNextRound(myPlayer)
                }
                if (!test) {
                    delay(Random.nextLong(1000L..1500L))
                }
            }
        }
    }

    override suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos> {
        val moves = round.getAllPiecesPos(role).first().flatMap { source ->
            round.getAvailableTargets(source).first().map { dest ->
                Move(source, dest, round.pieceAt(dest) != null)
            }
        }

        val blackPiece = round.getAllPiecesPos(Role.BLACK).first()
        val whitePiece = round.getAllPiecesPos(Role.WHITE).first()
        val seed = game.properties.seed
        val resp = client.post(
            endpoint + "/AI/" + if (role == Role.BLACK) "black" else "white"
        ) {
            contentType(Application.Json)
            setBody(buildJsonObject {
                put("move", buildJsonArray {
                    for (m in moves) {
                        add(buildJsonArray {
                            add(m.source.row)
                            add(m.source.col)
                            add(m.dest.row)
                            add(m.dest.col)
                            add(m.isCapture)
                        })
                    }
                })
                put("black_pieces", buildJsonArray {
                    for (p in blackPiece) {
                        add(buildJsonArray {
                            add(p.row)
                            add(p.col)
                        })
                    }
                })
                put("white_pieces", buildJsonArray {
                    for (p in whitePiece) {
                        add(buildJsonArray {
                            add(p.row)
                            add(p.col)
                        })
                    }
                })
                put("seed", buildJsonArray { add(seed) })
            })
        }
        val move = resp.body<List<Int>>()
        return BoardPos(move[0], move[1]) to BoardPos(move[2], move[3])
    }

    override suspend fun end() {
        myCoroutine.cancel()
    }

}


class AlgorithmAI(
    override val game: GameSession = GameSession.create(0),
    override val myPlayer: Player,
    private val parentCoroutineContext: CoroutineContext,
    private val test: Boolean = false,
//    private val endpoint: String = "http://home.him188.moe:4393"
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
                        if (!test) {
                            delay(Random.nextLong(1000L..1500L))
                        }
                        if (bestPos != null) {
                            session.move(bestPos.first, bestPos.second)
                        }
                        else {
                            val move = findRandomMove(session, currentRole)
                            session.move(move.first, move.second)
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
                if (!test) {
                    delay(Random.nextLong(1000L..1500L))
                }
            }
        }

    }

    override suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos>? {
        val board = createKeizarGraph(role, game)
        var minDistance = Int.MAX_VALUE
        var move: Pair<BoardPos, BoardPos>? = null
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                println(board[i][j].distance)
                println(board[i][j] is NormalNode)
            }
        }
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                if (game.currentRound.first().pieceAt(BoardPos(i, j)) == role && BoardPos(i, j) != game.properties.keizarTilePos) {
                    if (board[i][j] is NormalNode) {
                        if (board[i][j].distance in 1..< minDistance) {
                            val chosenParent = board[i][j].chosenParent
                            if (chosenParent != null) {
                                val target = chosenParent.position
                                if (target != game.properties.keizarTilePos || target == game.properties.keizarTilePos
                                    && game.currentRound.first().pieceAt(target) == role.other()) {
                                    minDistance = board[i][j].distance
                                    move = BoardPos(i, j) to target
                                }
                            }
                        }
                    }
                }
            }
        }
        print(minDistance)
        if (move != null) {
            println(move.first.row)
            println(move.first.col)
            println(move.second.row)
            println(move.second.col)
        }
        return move
    }

    override suspend fun end() {
        myCoroutine.cancel()
    }

}

