package org.keizar.server

import kotlinx.coroutines.CoroutineScope
import org.keizar.game.BoardProperties
import org.keizar.server.database.DatabaseManagerImpl
import org.keizar.server.modules.GameRoomsModuleImpl
import org.keizar.server.modules.AccountModuleImpl
import org.keizar.server.modules.SeedBankModuleImpl
import org.keizar.server.utils.AesEncoder
import org.keizar.server.utils.AuthTokenConfig
import org.keizar.server.utils.AuthTokenManagerImpl
import org.slf4j.Logger
import kotlin.time.Duration.Companion.days

class ServerContext(
    parentCoroutineScope: CoroutineScope,
    logger: Logger,
) {
    private val databaseManager = DatabaseManagerImpl()
    val authTokenManager = AuthTokenManagerImpl(
        config = AuthTokenConfig(
            secret = "d6yHBc5hXQrUjBKTK8Z3WFx7i6Zm6Ufm", // TODO: Change this to a secure secret
            expirationTime = 7.days.inWholeMilliseconds,
        ),
        encoder = AesEncoder()
    )

    val accounts = AccountModuleImpl(databaseManager, authTokenManager)
    val gameRooms = GameRoomsModuleImpl(parentCoroutineScope.coroutineContext, logger)
    val seedBank = SeedBankModuleImpl()
}

fun setupServerContext(coroutineScope: CoroutineScope, logger: Logger): ServerContext {
    return ServerContext(coroutineScope, logger)
}