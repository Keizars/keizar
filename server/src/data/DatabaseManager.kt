package org.keizar.server.data

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries
import org.keizar.server.data.local.InMemoryGameDataRepository
import org.keizar.server.data.local.InMemorySeedBankRepository
import org.keizar.server.data.local.InMemoryUserRepository
import org.keizar.server.data.models.GameDataModel
import org.keizar.server.data.models.SeedBankModel
import org.keizar.server.data.models.UserModel
import org.keizar.server.data.mongodb.KeizarCodecRegistry
import org.keizar.server.data.mongodb.MongoGameDataRepository
import org.keizar.server.data.mongodb.MongoSeedBankRepository
import org.keizar.server.data.mongodb.MongoUserRepository
import org.keizar.server.plugins.ServerJson

/**
 * [DatabaseManager] provides access to all the repositories of the database.
 * Every Repository class manages one table/collection in the database.
 * The structure of the table entries is defined in the `models` package.
 *
 * The repositories include:
 * - [UserRepository] for managing user's username, nickname, password hash, and avatar URL
 * - [SeedBankRepository] for managing user's saved seeds
 * - [GameDataRepository] for managing game statistics recordings
 *
 * Instance of a [DatabaseManager] is created in and accessible from the [ServerContext].
 *
 * Steps needed to add a new table/collection to the database:
 * 1. Create a new model in the `database.models` package representing the structure of the table entries
 * 2. Create a new repository interface in the `database` package, adding the necessary methods to manipulate the table
 * 3. Create and implement a new in-memory repository implementation in the `database.local` package
 * 5. Create and implement a new MongoDB repository implementation in the `database.mongodb` package (The collection will be automatically created in the remote database)
 * 6. Register the new repository in the [DatabaseManager] interface, and register the implementations in the [InMemoryDatabaseManagerImpl] and [MongoDatabaseManagerImpl] classes
 * 7. Access the new repository through the [DatabaseManager] instance in the [ServerContext]
 */
interface DatabaseManager {
    val user: UserRepository
    val seedBank: SeedBankRepository
    val gameData: GameDataRepository
    suspend fun initialize() {}
}

/**
 * [InMemoryDatabaseManagerImpl] is an implementation of [DatabaseManager]
 * that uses in-memory data structures mocking the production database environment.
 *
 * This is used for the automated tests and debugging during development.
 * Invoking the server with the environment variable `TESTING` set to true will use this implementation.
 */
class InMemoryDatabaseManagerImpl : DatabaseManager {
    override val user: UserRepository = InMemoryUserRepository()
    override val seedBank: SeedBankRepository = InMemorySeedBankRepository()
    override val gameData: GameDataRepository = InMemoryGameDataRepository()
}

/**
 * [MongoDatabaseManagerImpl] is the default implementation of [DatabaseManager] that connects
 * to a remote MongoDB database using the connection URL provided.
 *
 * Initialization of the database (e.g. creating indexes) is done in the `initialize` method.
 */
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

    override suspend fun initialize() {}

    override val user: UserRepository = MongoUserRepository(userTable)
    override val seedBank: SeedBankRepository = MongoSeedBankRepository(seedBankTable)
    override val gameData: GameDataRepository = MongoGameDataRepository(gameDataTable)

    suspend fun buildIndex() {
        seedBankTable.createIndex(
            keys = Indexes.compoundIndex(Indexes.text("userId"), Indexes.text("gameSeed")),
            options = IndexOptions().unique(true)
        )
        gameDataTable.createIndex(
            keys = Indexes.text("userId"),
            options = IndexOptions().unique(false)
        )
    }
}
