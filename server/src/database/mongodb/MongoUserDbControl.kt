package org.keizar.server.database.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.keizar.server.database.AbstractUserDbControl
import org.keizar.server.database.models.UserModel

class MongoUserDbControl(
    private val userTable: MongoCollection<UserModel>
) : AbstractUserDbControl() {

    override suspend fun addUser(userModel: UserModel): Boolean {
        return userTable.insertOne(userModel.sanitized()).wasAcknowledged()
    }

    override suspend fun update(
        userId: String,
        newUsername: String?,
        newNickname: String?,
        avatarUrl: String?
    ): Boolean {
        return userTable.updateOne(
            filter = Filters.eq("_id", userId),
            update = Updates.combine(listOfNotNull(
                newUsername?.let {
                    Updates.set("username", it.lowercase())
                },
                newNickname?.let {
                    Updates.set("nickname", it)
                },
                avatarUrl?.let {
                    Updates.set("avatarUrl", it)
                }
            )),
        ).matchedCount > 0
    }

    override suspend fun containsUsername(username: String): Boolean {
        return userTable.find(
            Filters.eq("username", username.lowercase())
        ).firstOrNull() != null
    }

    override suspend fun getUserById(userId: String): UserModel? {
        return userTable.find(
            Filters.eq("_id", userId)
        ).firstOrNull()
    }

    override suspend fun getUserByUsername(username: String): UserModel? {
        return userTable.find(
            Filters.eq("username", username.lowercase())
        ).firstOrNull()
    }
}