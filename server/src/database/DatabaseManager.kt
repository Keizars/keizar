package org.keizar.server.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import org.bson.UuidRepresentation
import org.keizar.server.database.local.InMemorySeedBankDbControl
import org.keizar.server.database.local.InMemoryUserDbControl
import org.keizar.server.database.models.SeedBankModel
import org.keizar.server.database.models.UserModel
import org.keizar.server.database.mongodb.MongoSeedBankDbControl
import org.keizar.server.database.mongodb.MongoUserDbControl

interface DatabaseManager {
    val user: UserDbControl
    val seedBank: SeedBankDbControl
}

class InMemoryDatabaseManagerImpl : DatabaseManager {
    override val user: UserDbControl = InMemoryUserDbControl()
    override val seedBank: SeedBankDbControl = InMemorySeedBankDbControl()
}

class MongoDatabaseManagerImpl : DatabaseManager {
    private val client = MongoClient.create(MongoClientSettings.builder().apply {
        applyConnectionString(ConnectionString(System.getenv("MONGODB_CONNECTION_STRING")))
        uuidRepresentation(UuidRepresentation.STANDARD)
    }.build())

    private val db = client.getDatabase("keizar-production")
    private val userTable = db.getCollection<UserModel>("users")
    private val seedBankTable = db.getCollection<SeedBankModel>("seed-bank")

    override val user: UserDbControl = MongoUserDbControl(userTable)
    override val seedBank: SeedBankDbControl = MongoSeedBankDbControl(seedBankTable)
}
