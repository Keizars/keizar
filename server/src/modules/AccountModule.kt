package org.keizar.server.modules

import io.ktor.http.ContentType
import org.keizar.server.database.DatabaseManager
import org.keizar.server.database.models.UserModel
import org.keizar.server.utils.AuthTokenManager
import org.keizar.utils.communication.LiteralChecker
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.User
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.util.UUID

interface AccountModule {
    suspend fun register(username: String, hash: ByteArray): AuthResponse
    suspend fun isUsernameTaken(username: String): Boolean
    suspend fun login(username: String, hash: ByteArray): AuthResponse
    suspend fun getUser(userId: UUID): User?
    suspend fun getUserByName(name: String): User?
    suspend fun getUserAvatar(uid: UUID): Pair<File, ContentType>?
    suspend fun uploadNewAvatar(uid: UUID, input: InputStream, contentType: ContentType): String
}

class AccountModuleImpl(
    private val database: DatabaseManager,
    private val authTokenManager: AuthTokenManager,
) : AccountModule {
    override suspend fun register(username: String, hash: ByteArray): AuthResponse {
        if (isUsernameTaken(username)) {
            return AuthResponse(AuthStatus.DUPLICATED_USERNAME)
        }

        val status = LiteralChecker.checkUsername(username)
        return if (status == AuthStatus.SUCCESS) {
            val userId = UUID.randomUUID()
           database.user.addUser(
               UserModel(
               id = userId.toString(),
               username = username,
               hash = hash.toString(Charsets.UTF_8),
           )
           )

            val token = authTokenManager.createToken(userId.toString())
            AuthResponse(status, token)
        } else {
            AuthResponse(status)
        }
    }

    override suspend fun isUsernameTaken(username: String): Boolean {
        return database.user.containsUsername(username)
    }

    override suspend fun login(username: String, hash: ByteArray): AuthResponse {
        val user = database.user.getUserByName(username) ?: return AuthResponse(AuthStatus.USER_NOT_FOUND)

        return if (user.hash != hash.toString(Charsets.UTF_8)) {
            AuthResponse(AuthStatus.WRONG_PASSWORD)
        } else {
            val userId = UUID.fromString(user.id)
            val token = authTokenManager.createToken(userId.toString())
            AuthResponse(AuthStatus.SUCCESS, token)
        }
    }

    override suspend fun getUser(userId: UUID): User? {
        val user = database.user.getUserById(userId.toString()) ?: return null
        return User(
            nickname = user.nickname ?: user.username,
            username = user.username,
            avatarUrl = "TODO: Not implemented yet",
        )
    }

    override suspend fun getUserByName(name: String): User? {
        val user = database.user.getUserByName(name) ?: return null
        return User(
            nickname = user.nickname ?: user.username,
            username = user.username,
            avatarUrl = "TODO: Not implemented yet",
        )
    }

    override suspend fun getUserAvatar(uid: UUID): Pair<File, ContentType>? {
        TODO("Not yet implemented")
    }

    override suspend fun uploadNewAvatar(uid: UUID, input: InputStream, contentType: ContentType): String {
        TODO("Not yet implemented")
    }
}
