package org.keizar.server.modules

import org.keizar.server.database.DatabaseManager
import org.keizar.server.utils.AuthTokenManager
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.account.AuthStatus
import java.util.UUID

interface AccountModule {
    fun register(username: String, hash: ByteArray): AuthResponse
    fun isUsernameTaken(username: String): Boolean
    fun login(username: String, hash: ByteArray): AuthResponse
}

class AccountModuleImpl(
    private val database: DatabaseManager,
    private val authTokenManager: AuthTokenManager,
) : AccountModule {
    override fun register(username: String, hash: ByteArray): AuthResponse {
        if (isUsernameTaken(username)) {
            return AuthResponse(AuthStatus.INVALID_USERNAME)
        }
        val userId = UUID.randomUUID()
        // TODO: connect database
        val token = authTokenManager.createToken(userId)
        return AuthResponse(AuthStatus.SUCCESS, token)
    }

    override fun isUsernameTaken(username: String): Boolean {
        // TODO("Not yet implemented")
        return false
    }

    override fun login(username: String, hash: ByteArray): AuthResponse {
        val userId = UUID.randomUUID()
        // TODO("Not yet implemented")
        val token = authTokenManager.createToken(userId)
        return AuthResponse(AuthStatus.SUCCESS, token)
    }
}
