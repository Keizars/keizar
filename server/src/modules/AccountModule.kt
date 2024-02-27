package org.keizar.server.modules

import io.ktor.http.ContentType
import org.keizar.server.database.DatabaseManager
import org.keizar.server.utils.AuthTokenManager
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.User
import java.io.File
import java.io.InputStream
import java.util.UUID

interface AccountModule {
    fun register(username: String, hash: ByteArray): AuthResponse
    fun isUsernameTaken(username: String): Boolean
    fun login(username: String, hash: ByteArray): AuthResponse
    fun getUser(userId: UUID): User?
    fun getUserByName(name: String): User?
    fun getUserAvatar(uid: UUID): Pair<File, ContentType>?
    fun uploadNewAvatar(uid: UUID, input: InputStream, contentType: ContentType): String
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
        // TODO: connect database
        return false
    }

    override fun login(username: String, hash: ByteArray): AuthResponse {
        val userId = UUID.randomUUID()
        // TODO: connect database
        val token = authTokenManager.createToken(userId)
        return AuthResponse(AuthStatus.SUCCESS, token)
    }

    override fun getUser(userId: UUID): User? {
        TODO()
    }

    override fun getUserByName(name: String): User? {
        TODO("Not yet implemented")
    }

    override fun getUserAvatar(uid: UUID): Pair<File, ContentType>? {
        TODO("Not yet implemented")
    }

    override fun uploadNewAvatar(uid: UUID, input: InputStream, contentType: ContentType): String {
        TODO("Not yet implemented")
    }
}
