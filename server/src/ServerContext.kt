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
import java.security.SecureRandom
import kotlin.time.Duration.Companion.days

/**
 * The [ServerContext] class represents the context for the server application, providing access
 * to various modules and managers needed for its operation.
 */
class ServerContext(
    parentCoroutineScope: CoroutineScope,
    logger: Logger,
) {
    private val databaseManager = if (System.getenv("TESTING") == "true") {
        InMemoryDatabaseManagerImpl()
    } else {
        MongoDatabaseManagerImpl(
            connection = System.getenv("MONGODB_CONNECTION_STRING")
                ?: throw RuntimeException("Missing MONGODB_CONNECTION_STRING environment variable")
        )
    }

    val authTokenManager = AuthTokenManagerImpl(
        config = AuthTokenConfig(
            secret = System.getenv("TOKEN_SECRET") ?: generateSecureRandomString(),
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

private fun generateSecureRandomString(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return bytes.toString(Charsets.UTF_8)
}