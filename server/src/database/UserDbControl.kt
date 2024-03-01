package org.keizar.server.database

import org.keizar.server.database.models.UserModel

interface UserDbControl {
    suspend fun addUser(userModel: UserModel): Boolean
    suspend fun update(
        userId: String,
        newUsername: String? = null,
        newNickname: String? = null,
        avatarUrl: String? = null,
    ): Boolean

    suspend fun containsUsername(username: String): Boolean
    suspend fun getUserById(userId: String): UserModel?
    suspend fun getUserByUsername(username: String): UserModel?
}

abstract class AbstractUserDbControl : UserDbControl {
    protected open fun UserModel.sanitized() = copy(username = username.lowercase())
}