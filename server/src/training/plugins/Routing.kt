package org.keizar.server.training.plugins

import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.keizar.server.training.RuleEngineAdaptor
import org.keizar.server.training.ServerContext
import org.keizar.game.Move
import org.keizar.game.Role

fun Application.configureRouting(context: ServerContext) {
    routing {
        get("/board") {
            call.respond(RuleEngineAdaptor.encodeBoard(context.boardProperties))
        }
        post("/moves/white") {
            val boardInput = call.receive<AIBoardData>()
            val moves: List<Move> = RuleEngineAdaptor.getPossibleMoves(
                context.boardProperties,
                boardInput.board,
                Role.WHITE
            )
            call.respond(moves)
        }
        post("/moves/black") {
            val boardInput = call.receive<AIBoardData>()
            val moves: List<Move> = RuleEngineAdaptor.getPossibleMoves(
                context.boardProperties,
                boardInput.board,
                Role.BLACK
            )
            call.respond(moves)
        }
    }
}
