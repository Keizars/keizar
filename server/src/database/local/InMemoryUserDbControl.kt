package org.keizar.server.database.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.keizar.server.database.models.UserModel
import org.keizar.server.database.UserDbControl

class InMemoryUserDbControl : UserDbControl {
    private val _data: MutableList<UserModel> = mutableListOf()
    private val mutex = Mutex()
    private suspend inline fun <T> data(crossinline block: MutableList<UserModel>.() -> T): T {
        mutex.withLock { return _data.block() }
    }

    override suspend fun addUser(userModel: UserModel): Boolean {
        return data { add(userModel) }
    }

    override suspend fun updateUsername(userId: String, newUsername: String): Boolean {
        return data {
            find { it.id == userId }?.let {
                remove(it)
                add(it.copy(username = newUsername))
                true
            } ?: false
        }
    }

    override suspend fun updateNickname(userId: String, newNickname: String): Boolean {
        return data {
            find { it.id == userId }?.let {
                remove(it)
                add(it.copy(nickname = newNickname))
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