package org.keizar.android.data

import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.keizar.android.persistent.TokenRepository
import org.keizar.android.ui.foundation.BackgroundScope
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.keizar.client.services.UserService
import org.keizar.utils.communication.account.User
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds

/**
 * [SessionManager] manages the login session for the user.
 *
 * ## Obtaining a [SessionManager] instance
 *
 * A [SessionManager] should be singleton (created via `SessionManager()`) and lives the entire application lifecycle. Hence it should be injected:
 *
 * ```
 * val sessionManager: SessionManager by inject()
 * ```
 *
 * ## Working with login sessions
 *
 * ### Retrieving the login session
 *
 * Use [token] to listen on the current token, and [isLoggedIn] to check if the user is logged in.
 *
 * ### Updating a login session
 *
 * When the player successfully logs in, [SessionManager.setToken] should be called. [token] will then emit new tokens.
 *
 * ## Session persistence
 *
 * [SessionManager] automatically manages the persistence of the token:
 * - Writes the token to the persistent storage when [SessionManager.setToken] is called.
 * - Reads the token from the persistent storage when it is instantiated.
 */
interface SessionManager : AutoCloseable {
    /**
     * Current user token. `null` if user has not yet logged in.
     * It is preferred to use [isLoggedIn] to check if the user is logged in.
     */
    val token: SharedFlow<String?>

    /**
     * Whether the user is logged in.
     */
    val isLoggedIn: Flow<Boolean>

    /**
     * Information about the user currently logged in.
     * `null` if user has not yet logged in.
     */
    val self: StateFlow<User?>

    /**
     * Invalidates the current token.
     *
     * If there isn't a token, this method does nothing.
     */
    suspend fun invalidateToken()

    /**
     * Replaces the current token with [token].
     */
    suspend fun setToken(token: String)
}

/**
 * Creates a new [SessionManager] instance.
 */
fun SessionManager(): SessionManager = SessionManagerImpl()

private class SessionManagerImpl : SessionManager, KoinComponent, HasBackgroundScope by BackgroundScope() {
    private val tokenRepository: TokenRepository by inject()
    private val userService: UserService by inject()

    private val logger = logger(SessionManager::class)

    override val token: SharedFlow<String?> = tokenRepository.token.shareInBackground(started = SharingStarted.Eagerly)

    /**
     * Current user's information.
     */
    override val self: StateFlow<User?> = token.transformLatest {
        if (it == null) {
            emit(null)
            return@transformLatest
        }
        while (true) {
            try {
                emit(userService.self())
                return@transformLatest
            } catch (e: Exception) {
                logger.error(e) { "Failed to get self" }
                emit(null)
                delay(5.seconds)
                continue
            }
        }
    }.stateInBackground(started = SharingStarted.Eagerly)

    override val isLoggedIn: Flow<Boolean> = self.map { it != null }

    override suspend fun invalidateToken() {
        logger.info { "Invalidating token" }
        tokenRepository.setToken(null)
    }

    override suspend fun setToken(token: String) {
        logger.info { "Updating token with ${token.take(16)}..." }
        tokenRepository.setToken(token)
    }

    override fun close() {
        backgroundScope.cancel()
    }
}