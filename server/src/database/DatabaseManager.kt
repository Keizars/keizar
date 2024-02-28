package org.keizar.server.database

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*

interface DatabaseManager {

}

class DatabaseManagerImpl : DatabaseManager {

}



interface DynamoDbDao<T> {
    fun getItem(key: Map<String, AttributeValue>): T?
    fun putItem(item: T)
    fun deleteItem(key: Map<String, AttributeValue>)
    fun getAllItems(): List<T>
}

class UserDao : DynamoDbDao<User> {
    private val dynamoDbClient: DynamoDbClient = createClient()
    private val tableName = "users"
    override fun getItem(key: Map<String, AttributeValue>): User? {
        val request = GetItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build()

        val response = dynamoDbClient.getItem(request)
        return if (response.hasItem()) {
            User(
                userId = response.item()["user_id"]!!.s(),
                userName = response.item()["user_name"]!!.s()
            )
        } else {
            null
        }
    }

    override fun putItem(item: User) {
        val itemValues = mapOf(
            "user_name" to AttributeValue.builder().s(item.userName).build()
        )

        val request = PutItemRequest.builder()
            .tableName(tableName)
            .item(itemValues)
            .build()

        try {
            dynamoDbClient.putItem(request)
            println("Item with userId $itemValues successfully added.")
        } catch (e: Exception) {
            println("Error deleting item: ${e.message}")
        }
    }

    override fun deleteItem(key: Map<String, AttributeValue>) {

        val deleteItemRequest = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build()

        try {
            dynamoDbClient.deleteItem(deleteItemRequest)
            println("Item with userId $key successfully deleted.")
        } catch (e: Exception) {
            println("Error deleting item: ${e.message}")
        }
    }


    override fun getAllItems(): List<User> {
        val scanRequest = ScanRequest.builder()
            .tableName(tableName)
            .build()
        val response = dynamoDbClient.scan(scanRequest)
        return response.items().map {
            User(
                userId = it["user_id"]!!.s(),
                userName = it["user_name"]!!.s()
            )
        }
    }
}

class GamesDao : DynamoDbDao<Games> {
    private val dynamoDbClient: DynamoDbClient = createClient()
    private val tableName = "games"
    override fun getItem(key: Map<String, AttributeValue>): Games? {
        val request = GetItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build()

        val response = dynamoDbClient.getItem(request)
        return if (response.hasItem()) {
            Games(
                gameId = response.item()["game_id"]!!.s(),
                userName1 = response.item()["user_name1"]!!.s(),
                userName2 = response.item()["user_name2"]!!.s()
            )
        } else {
            null
        }
    }

    override fun putItem(item: Games) {
        val itemValues = mapOf(
            "game_id" to AttributeValue.builder().s(item.gameId).build(),
            "user_name1" to AttributeValue.builder().s(item.userName1).build(),
            "user_name2" to AttributeValue.builder().s(item.userName2).build()
        )

        val request = PutItemRequest.builder()
            .tableName(tableName)
            .item(itemValues)
            .build()

        try {
            dynamoDbClient.putItem(request)
            println("Item with gameId $itemValues successfully added.")
        } catch (e: Exception) {
            println("Error deleting item: ${e.message}")
        }
    }

    override fun deleteItem(key: Map<String, AttributeValue>) {

        val deleteItemRequest = DeleteItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build()

        try {
            dynamoDbClient.deleteItem(deleteItemRequest)
            println("Item with gameId $key successfully deleted.")
        } catch (e: Exception) {
            println("Error deleting item: ${e.message}")
        }
    }

    override fun getAllItems(): List<Games> {
        val scanRequest = ScanRequest.builder()
            .tableName(tableName)
            .build()
        val response = dynamoDbClient.scan(scanRequest)
        return response.items().map {
            Games(
                gameId = it["game_id"]!!.s(),
                userName1 = it["user_name1"]!!.s(),
                userName2 = it["user_name2"]!!.s()
            )
        }
    }

    fun getGamesByUserId(userId: String): List<Games> {
        val scanRequest = ScanRequest.builder()
            .tableName(tableName)
            .filterExpression("user_name1 = :user_name or user_name2 = :user_name")
            .expressionAttributeValues(mapOf(":user_name" to AttributeValue.builder().s(userId).build()))
            .build()
        val response = dynamoDbClient.scan(scanRequest)
        return response.items().map {
            Games(
                gameId = it["game_id"]!!.s(),
                userName1 = it["user_name1"]!!.s(),
                userName2 = it["user_name2"]!!.s()
            )
        }
    }

}
