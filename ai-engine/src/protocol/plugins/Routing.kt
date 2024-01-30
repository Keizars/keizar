package org.keizar.aiengine.protocol.plugins

import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.keizar.aiengine.protocol.RuleEngineAdaptor
import org.keizar.aiengine.protocol.ServerContext
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.Role

fun Application.configureRouting(context: ServerContext) {
    routing {
        get("/board") {
            call.respond(RuleEngineAdaptor.encodeBoard(context.boardProperties))
        }
        post("/moves/white") {
            val boardInput = call.receive<List<List<Int>>>()
            val moves: List<Move> =
                RuleEngineAdaptor.getPossibleMoves(context.boardProperties, boardInput, Role.WHITE)
            call.respond(moves)
        }
        post("/moves/black") {
            val boardInput = call.receive<List<List<Int>>>()
            val moves: List<Move> =
                RuleEngineAdaptor.getPossibleMoves(context.boardProperties, boardInput, Role.BLACK)
            call.respond(moves)
        }
    }
}
