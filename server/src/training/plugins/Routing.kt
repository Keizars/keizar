package org.keizar.server.training.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import org.keizar.game.BoardProperties
import org.keizar.game.Move
import org.keizar.game.Role
import org.keizar.server.training.RuleEngineAdaptor
import org.keizar.server.ServerContext

fun Application.configureTrainingRouting(context: ServerContext) {
    routing {
        put("/board/{seed}") {
            val seed = call.parameters["seed"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid seed")
            context.trainingBoardProperties = BoardProperties.getStandardProperties(seed)
            call.respond(RuleEngineAdaptor.encodeBoard(context.trainingBoardProperties))
        }
        post("/moves/white") {
            val boardInput = call.receive<AIBoardData>()
            val moves: List<Move> = RuleEngineAdaptor.getPossibleMoves(
                context.trainingBoardProperties,
                boardInput.board,
                Role.WHITE
            )
            call.respond(moves)
        }
        post("/moves/black") {
            val boardInput = call.receive<AIBoardData>()
            val moves: List<Move> = RuleEngineAdaptor.getPossibleMoves(
                context.trainingBoardProperties,
                boardInput.board,
                Role.BLACK
            )
            call.respond(moves)
        }
    }
}
