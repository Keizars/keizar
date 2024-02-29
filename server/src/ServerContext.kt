package org.keizar.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.keizar.server.database.InMemoryDatabaseManagerImpl
import org.keizar.server.database.MongoDatabaseManagerImpl
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
    private val databaseManager = if (System.getenv("TESTING") == "true") {
        InMemoryDatabaseManagerImpl()
    } else {
        MongoDatabaseManagerImpl(connection = System.getenv("MONGODB_CONNECTION_STRING"))
    }

    val authTokenManager = AuthTokenManagerImpl(
        config = AuthTokenConfig(
            secret = "d6yHBc5hXQrUjBKTK8Z3WFx7i6Zm6Ufm", // TODO: Change this to a secure secret
            expirationTime = 7.days.inWholeMilliseconds,
        ),
        encoder = AesEncoder()
    )

    init {
        parentCoroutineScope.launch { databaseManager.initialize() }
    }

    val gameRooms = GameRoomsModuleImpl(parentCoroutineScope.coroutineContext, logger)
    val accounts = AccountModuleImpl(databaseManager, authTokenManager)
    val seedBank = SeedBankModuleImpl(databaseManager)
}

fun setupServerContext(coroutineScope: CoroutineScope, logger: Logger): ServerContext {
    return ServerContext(coroutineScope, logger)
}