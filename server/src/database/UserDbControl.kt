package org.keizar.server.database

import org.keizar.server.database.models.UserModel

interface UserDbControl {
    suspend fun addUser(userModel: UserModel): Boolean
    suspend fun updateUsername(userId: String, newUsername: String): Boolean
    suspend fun updateNickname(userId: String, newNickname: String): Boolean
    suspend fun containsUsername(username: String): Boolean
    suspend fun getUserById(userId: String): UserModel?
    suspend fun getUserByName(username: String): UserModel?
}