package org.keizar.server

import io.ktor.server.application.Application
import kotlinx.coroutines.CoroutineScope
import org.keizar.game.BoardProperties
import org.keizar.server.gameroom.GameRoomManagerImpl
import org.slf4j.Logger

class ServerContext(
    parentCoroutineScope: CoroutineScope,
    logger: Logger,
) {
    val gameRoomManager = GameRoomManagerImpl(parentCoroutineScope.coroutineContext, logger)

    var trainingBoardProperties = BoardProperties.getStandardProperties()
}

fun setupServerContext(coroutineScope: CoroutineScope, logger: Logger): ServerContext {
    return ServerContext(coroutineScope, logger)
}