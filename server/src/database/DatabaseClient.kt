package org.keizar.server.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.flow.toList
import org.bson.UuidRepresentation
import org.keizar.server.database.models.UserModel

interface DatabaseClient {
    fun <T> putItem(item: T)
    fun <T> deleteItem(key: Map<String, String>)
    fun <T> getAllItems(): List<T>
}

private suspend fun main() {
    val client =
        MongoClient.create(MongoClientSettings.builder().apply {
            applyConnectionString(ConnectionString(System.getenv("MONGODB_CONNECTION_STRING")))
            uuidRepresentation(UuidRepresentation.STANDARD)
        }.build())


    client.listDatabaseNames().toList().forEach { println(it) }

    val db = client.getDatabase("keizar-production")

    val userModel = db.getCollection<UserModel>("users")

    println(
        userModel.insertOne(
            UserModel(
                id = "test",
                username = "",
                hash = "",
            )
        )
    )

    userModel.updateOne(
        filter = Filters.eq("id", "test"),
        update = Updates.set("id", "test2")
    )

    userModel.find(
        Filters.and(
            Filters.eq("id", "test"),
            Filters.gt("date", "2013-")
        )
    ).toList()
}