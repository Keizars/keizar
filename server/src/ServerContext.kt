package org.keizar.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.keizar.server.database.DatabaseManager
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
import org.keizar.server.utils.AuthTokenManager
import org.keizar.server.utils.AuthTokenManagerImpl
import org.slf4j.Logger
import java.security.SecureRandom
import kotlin.time.Duration.Companion.days

/**
 * The [ServerContext] class represents a context object for the server application in order to
 * achieve dependency injection. It contains all the service modules used by the server.
 *
 * Instances can be created using the constructor or the [setupServerContext] function.
 *
 * @param parentCoroutineScope The [CoroutineScope] used to launch coroutines in the server application.
 * @param logger The [org.slf4j.Logger] instance used by the server application to log messages.
 * @param env The environment variables used by the server application.
 */
class ServerContext internal constructor(
    parentCoroutineScope: CoroutineScope,
    logger: Logger,
    env: EnvironmentVariables,
) {
    /**
     * The [DatabaseManager] instance used by the service modules to interact with the database.
     */
    private val databaseManager: DatabaseManager = if (env.testing) {
        InMemoryDatabaseManagerImpl()
    } else {
        MongoDatabaseManagerImpl(
            connection = env.mongoDbConnectionString
                ?: throw RuntimeException("Missing MONGODB_CONNECTION_STRING environment variable")
        )
    }

    /**
     * The [AuthTokenManager] instance used by the security plugin and the [accounts] module
     * to create and validate authentication tokens.
     */
    val authTokenManager: AuthTokenManager = AuthTokenManagerImpl(
        config = AuthTokenConfig(
            secret = env.tokenSecret?.toByteArray() ?: generateSecureRandomBytes(),
            expirationTime = 7.days.inWholeMilliseconds,
        ),
        encoder = AesEncoder()
    )

    /**
     * The [AvatarStorage] instance used by the [accounts] module to store and retrieve user avatars.
     */
    private val avatarStorage: AvatarStorage = if (env.testing) {
        InMemoryAvatarStorage()
    } else {
        AwsAvatarStorage()
    }

    init {
        parentCoroutineScope.launch { databaseManager.initialize() }
    }

    /**
     * The service modules of the server application, containing the business logic
     * of the server used by the HTTP routing layer.
     * Uses the [databaseManager] to interact with the database.
     */
    val gameRooms = GameRoomsModuleImpl(parentCoroutineScope.coroutineContext, logger)
    val seedBank = SeedBankModuleImpl(databaseManager)
    val accounts = AccountModuleImpl(databaseManager, authTokenManager, avatarStorage)
    val gameData = GameDataModuleImpl(databaseManager)
}

/**
 * Function to create a [ServerContext] instance.
 *
 * @param coroutineScope The [CoroutineScope] used to launch coroutines in the server application.
 * @param logger The [org.slf4j.Logger] instance used by the server application to log messages.
 * @param env The environment variables used by the server application. By default, it reads the system environment variables.
 */
internal fun setupServerContext(
    coroutineScope: CoroutineScope,
    logger: Logger,
    env: EnvironmentVariables = EnvironmentVariables(),
): ServerContext {
    return ServerContext(coroutineScope, logger, env)
}

/**
 * Function that generates a secure random 32-byte array to be used as a secret for the [AuthTokenManager].
 */
private fun generateSecureRandomBytes(): ByteArray {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return bytes
}