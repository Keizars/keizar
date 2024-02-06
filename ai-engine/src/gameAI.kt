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
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
            game.currentRound.flatMapLatest { it.winner }.collect {
                combine(game.currentRole(myPlayer), game.currentRound) { myRole, session ->
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

class Q_table_AI(
    override val game: GameSession,
    override val myPlayer: Player,
    private val parentCoroutineContext: CoroutineContext,
    private val test: Boolean = false
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
            game.currentRound.flatMapLatest { it.winner }.collect {
                combine(game.currentRole(myPlayer), game.currentRound) { myRole, session ->
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
        val resp = client.post("http://localhost:5000/AI/" + if (role == Role.BLACK) "black" else "white") {
            contentType(Application.Json)
            setBody(buildJsonObject {
                put("move", buildJsonArray { for (m in moves) {
                    add(buildJsonArray {
                        add(m.source.row)
                        add(m.source.col)
                        add(m.dest.row)
                        add(m.dest.col)
                        add(m.isCapture)
                    })
                } })
                put("black_pieces", buildJsonArray { for (p in blackPiece) {
                    add(buildJsonArray {
                        add(p.row)
                        add(p.col)
                    })
                } })
                put("white_pieces", buildJsonArray { for (p in whitePiece) {
                    add(buildJsonArray {
                        add(p.row)
                        add(p.col)
                    })
                } })
            })
        }
        val move = resp.body<List<Int>>()
        return BoardPos(move[0], move[1]) to BoardPos(move[2], move[3])
    }

    override suspend fun end() {
        myCoroutine.cancel()
    }
}

