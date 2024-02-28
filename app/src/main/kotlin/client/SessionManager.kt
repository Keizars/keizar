package org.keizar.android.client

import kotlinx.coroutines.flow.StateFlow
import org.keizar.android.persistent.TokenRepository
import org.keizar.android.ui.foundation.BackgroundScope
import org.keizar.android.ui.foundation.HasBackgroundScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SessionManager : KoinComponent, HasBackgroundScope by BackgroundScope() {
    private val tokenRepository: TokenRepository by inject()

    val token: StateFlow<String?> = tokenRepository.token.stateInBackground()

    suspend fun invalidateToken() {
        tokenRepository.setToken(null)
    }

    suspend fun setToken(token: String) {
        tokenRepository.setToken(token)
    }
}