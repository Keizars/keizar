package org.keizar.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.keizar.server.database.InMemoryDatabaseManagerImpl
import org.keizar.server.database.MongoDatabaseManagerImpl
import org.keizar.server.modules.AccountModuleImpl
import org.keizar.server.modules.AvatarStorage
import org.keizar.server.modules.AwsAvatarStorage
import org.keizar.server.modules.GameDataModuleImpl
import org.keizar.server.modules.GameRoomsModuleImpl
import org.keizar.server.modules.InMemoryAvatarStorage
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
    env: EnvironmentVariables,
) {
    private val databaseManager = if (env.testing) {
        InMemoryDatabaseManagerImpl()
    } else {
        MongoDatabaseManagerImpl(
            connection = env.mongoDbConnectionString
                ?: throw RuntimeException("Missing MONGODB_CONNECTION_STRING environment variable")
        )
    }

    val authTokenManager = AuthTokenManagerImpl(
        config = AuthTokenConfig(
            secret = env.tokenSecret?.toByteArray() ?: generateSecureRandomBytes(),
            expirationTime = 7.days.inWholeMilliseconds,
        ),
        encoder = AesEncoder()
    )

    init {
        parentCoroutineScope.launch { databaseManager.initialize() }
    }

    private val avatarStorage: AvatarStorage = if (env.testing) {
        InMemoryAvatarStorage()
    } else {
        AwsAvatarStorage()
    }

    val gameRooms = GameRoomsModuleImpl(parentCoroutineScope.coroutineContext, logger)
    val seedBank = SeedBankModuleImpl(databaseManager)
    val accounts = AccountModuleImpl(databaseManager, authTokenManager, avatarStorage)
    val gameData = GameDataModuleImpl(databaseManager)
}

fun setupServerContext(
    coroutineScope: CoroutineScope,
    logger: Logger,
    env: EnvironmentVariables = EnvironmentVariables(),
): ServerContext {
    return ServerContext(coroutineScope, logger, env)
}

private fun generateSecureRandomBytes(): ByteArray {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return bytes
}