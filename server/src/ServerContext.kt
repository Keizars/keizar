package org.keizar.server

import kotlinx.coroutines.CoroutineScope
import org.keizar.game.BoardProperties
import org.keizar.server.gameroom.GameRoomManagerImpl

class ServerContext(
    parentCoroutineScope: CoroutineScope,
) {
    var trainingBoardProperties = BoardProperties.getStandardProperties()
    val gameRoomManager = GameRoomManagerImpl(parentCoroutineScope)
}

fun setupServerContext(coroutineScope: CoroutineScope): ServerContext {
    return ServerContext(coroutineScope)
}