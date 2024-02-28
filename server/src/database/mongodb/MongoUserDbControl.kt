package org.keizar.server.database.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.keizar.server.database.UserDbControl
import org.keizar.server.database.models.UserModel

class MongoUserDbControl(
    private val userTable: MongoCollection<UserModel>
) : UserDbControl {

    override suspend fun addUser(userModel: UserModel): Boolean {
        return userTable.insertOne(userModel).wasAcknowledged()
    }

    override suspend fun updateUsername(userId: String, newUsername: String): Boolean {
        return userTable.updateOne(
            filter = Filters.eq("id", userId),
            update = Updates.set("username", newUsername)
        ).matchedCount > 0
    }

    override suspend fun updateNickname(userId: String, newNickname: String): Boolean {
        return userTable.updateOne(
            filter = Filters.eq("id", userId),
            update = Updates.set("nickname", newNickname)
        ).matchedCount > 0
    }

    override suspend fun containsUsername(username: String): Boolean {
        return userTable.find(
            Filters.eq("username", username)
        ).firstOrNull() != null
    }

    override suspend fun getUserById(userId: String): UserModel? {
        return userTable.find(
            Filters.eq("id", userId)
        ).firstOrNull()
    }

    override suspend fun getUserByName(username: String): UserModel? {
        return userTable.find(
            Filters.eq("username", username)
        ).firstOrNull()
    }
}