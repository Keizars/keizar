package org.keizar.server.data

import org.keizar.server.data.models.UserModel

interface UserRepository {
    suspend fun addUser(userModel: UserModel): Boolean
    suspend fun update(
        userId: String,
        newUsername: String? = null,
        newNickname: String? = null,
        passwordHash: String? = null,
        avatarUrl: String? = null,
    ): Boolean

    suspend fun containsUsername(username: String): Boolean
    suspend fun getUserById(userId: String): UserModel?
    suspend fun getUserByUsername(username: String): UserModel?
}

abstract class AbstractUserRepository : UserRepository {
    protected open fun UserModel.sanitized() = copy(username = username.lowercase())
}