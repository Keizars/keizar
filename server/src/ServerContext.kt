package org.keizar.server

import kotlinx.coroutines.CoroutineScope
import org.keizar.game.BoardProperties
import org.keizar.server.database.DatabaseManagerImpl
import org.keizar.server.gameroom.GameRoomManagerImpl
import org.keizar.server.modules.AccountModuleImpl
import org.keizar.server.utils.AesEncoder
import org.keizar.server.utils.AuthTokenConfig
import org.keizar.server.utils.AuthTokenManagerImpl
import org.slf4j.Logger
import kotlin.time.Duration.Companion.days

class ServerContext(
    parentCoroutineScope: CoroutineScope,
    logger: Logger,
) {
    val gameRoomManager = GameRoomManagerImpl(parentCoroutineScope.coroutineContext, logger)
    private val databaseManager = DatabaseManagerImpl()

    val authTokenManager = AuthTokenManagerImpl(
        config = AuthTokenConfig(
            secret = "HolgerPirk", // TODO: Change this to a secure secret
            expirationTime = 7.days.inWholeMilliseconds,
        ),
        encoder = AesEncoder()
    )
    val accounts = AccountModuleImpl(databaseManager, authTokenManager)


    var trainingBoardProperties = BoardProperties.getStandardProperties()
}

fun setupServerContext(coroutineScope: CoroutineScope, logger: Logger): ServerContext {
    return ServerContext(coroutineScope, logger)
}