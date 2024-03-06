package org.keizar.server.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries
import org.keizar.server.database.local.InMemoryGameDataRepository
import org.keizar.server.database.local.InMemorySeedBankRepository
import org.keizar.server.database.local.InMemoryUserRepository
import org.keizar.server.database.models.GameDataModel
import org.keizar.server.database.models.SeedBankModel
import org.keizar.server.database.models.UserModel
import org.keizar.server.database.mongodb.KeizarCodecRegistry
import org.keizar.server.database.mongodb.MongoGameDataRepository
import org.keizar.server.database.mongodb.MongoSeedBankRepository
import org.keizar.server.database.mongodb.MongoUserRepository
import org.keizar.server.plugins.ServerJson

/**
 * [DatabaseManager] is an interface that provides access to different repositories in the database.
 */
interface DatabaseManager {
    val user: UserRepository
    val seedBank: SeedBankRepository
    val gameData: GameDataRepository
    suspend fun initialize() {}
}

class InMemoryDatabaseManagerImpl : DatabaseManager {
    override val user: UserRepository = InMemoryUserRepository()
    override val seedBank: SeedBankRepository = InMemorySeedBankRepository()
    override val gameData: GameDataRepository = InMemoryGameDataRepository()
}

class MongoDatabaseManagerImpl(
    connection: String
) : DatabaseManager {
    private val client = MongoClient.create(MongoClientSettings.builder().apply {
        applyConnectionString(ConnectionString(connection))
        codecRegistry(
            CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                KeizarCodecRegistry(ServerJson),
            )
        )
        uuidRepresentation(UuidRepresentation.STANDARD)
    }.build())

    private val db = client.getDatabase("keizar-production")
    private val userTable = db.getCollection<UserModel>("users")
    private val seedBankTable = db.getCollection<SeedBankModel>("seeds")
    private val gameDataTable = db.getCollection<GameDataModel>("collections")

    override suspend fun initialize() {
        seedBankTable.createIndex(
            keys = Indexes.compoundIndex(Indexes.text("userId"), Indexes.text("gameSeed")),
            options = IndexOptions().unique(true)
        )
    }

    override val user: UserRepository = MongoUserRepository(userTable)
    override val seedBank: SeedBankRepository = MongoSeedBankRepository(seedBankTable)
    override val gameData: GameDataRepository = MongoGameDataRepository(gameDataTable)
}
