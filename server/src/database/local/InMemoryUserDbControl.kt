package org.keizar.server.database.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.server.database.UserDbControl
import org.keizar.server.database.models.UserModel

class InMemoryUserDbControl : UserDbControl {
    private val _data: MutableList<UserModel> = mutableListOf()
    private val mutex = Mutex()
    private suspend inline fun <T> data(crossinline block: MutableList<UserModel>.() -> T): T {
        mutex.withLock { return _data.block() }
    }

    override suspend fun addUser(userModel: UserModel): Boolean {
        return data { add(userModel) }
    }

    override suspend fun update(
        userId: String,
        newUsername: String?,
        newNickname: String?,
        avatarUrl: String?
    ): Boolean {
        return data {
            find { it.id == userId }?.let {
                remove(it)
                add(
                    it.copy(
                        username = newUsername ?: it.username,
                        nickname = newNickname ?: it.nickname,
                        avatarUrl = avatarUrl ?: it.avatarUrl
                    )
                )
                true
            } ?: false
        }
    }

    override suspend fun containsUsername(username: String): Boolean {
        return data { any { it.username == username } }
    }

    override suspend fun getUserById(userId: String): UserModel? {
        return data { find { it.id == userId } }
    }

    override suspend fun getUserByName(username: String): UserModel? {
        return data { find { it.username == username } }
    }
}