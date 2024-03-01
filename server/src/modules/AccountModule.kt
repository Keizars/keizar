package org.keizar.server.modules

import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.keizar.server.database.DatabaseManager
import org.keizar.server.database.models.UserModel
import org.keizar.server.utils.AuthTokenManager
import org.keizar.utils.communication.LiteralChecker
import org.keizar.utils.communication.account.AuthResponse
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.User
import java.io.File
import java.io.InputStream
import java.util.UUID

interface AccountModule {
    suspend fun register(username: String, hash: ByteArray): AuthResponse
    suspend fun isUsernameTaken(username: String): Boolean
    suspend fun login(username: String, hash: ByteArray): AuthResponse
    suspend fun getUser(userId: UUID): User?
    suspend fun getUserByName(name: String): User?
    suspend fun uploadNewAvatar(uid: UUID, input: InputStream, contentType: ContentType)
}

class AccountModuleImpl(
    private val database: DatabaseManager,
    private val authTokenManager: AuthTokenManager,
    private val avatarStorage: AvatarStorage,
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
            avatarUrl = user.avatarUrl ?: ""
        )
    }

    override suspend fun getUserByName(name: String): User? {
        val user = database.user.getUserByName(name) ?: return null
        return User(
            nickname = user.nickname ?: user.username,
            username = user.username,
            avatarUrl = user.avatarUrl ?: "",
        )
    }

//    override suspend fun getUserAvatar(uid: UUID): Pair<File, ContentType>? {
//        val user = database.user.getUserById(uid.toString()) ?: return null
//        return avatarStorage.uploadAvatar()
//    }

    override suspend fun uploadNewAvatar(uid: UUID, input: InputStream, contentType: ContentType) {
        val user = database.user.getUserById(uid.toString()) ?: throw IllegalArgumentException("User not found")
        val file = withContext(Dispatchers.IO) {
            File.createTempFile("avatar", "tmp").apply {
                this.outputStream().use { input.copyTo(it) }
            }
        }
        val newUrl = avatarStorage.uploadAvatar(uid.toString(), file, contentType.toString())
        withContext(Dispatchers.IO) {
            file.delete()
        }

        database.user.update(
            user.id,
            avatarUrl = newUrl
        )
    }
}
