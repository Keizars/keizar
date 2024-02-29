package org.keizar.android.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.logger
import org.keizar.android.persistent.TokenRepository
import org.keizar.android.ui.foundation.BackgroundScope
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SessionManager : KoinComponent, HasBackgroundScope by BackgroundScope() {
    private val tokenRepository: TokenRepository by inject()
    private val logger = logger(SessionManager::class)

    val token: SharedFlow<String?> = tokenRepository.token.shareInBackground(started = SharingStarted.Eagerly)
    val isLoggedIn: Flow<Boolean> = token.map { it != null }

    suspend fun invalidateToken() {
        logger.info { "Invalidating token" }
        tokenRepository.setToken(null)
    }

    suspend fun setToken(token: String) {
        logger.info { "Updating token with ${token.take(16)}..." }
        tokenRepository.setToken(token)
    }
}