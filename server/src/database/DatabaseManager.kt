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

fun createClient(): DynamoDbClient {
    val awsAccessKeyId = System.getenv("AWS_ACCESS_KEY_ID")
    val awsSecretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY")
    val awsCreds = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)

    return DynamoDbClient.builder()
        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        .region(Region.EU_WEST_1)
        .build()
}

interface DynamoDbDao<T> {
    fun getItem(key: Map<String, AttributeValue>): T?
    fun putItem(item: T)
    fun deleteItem(key: Map<String, AttributeValue>)
    fun updateItem(key: Map<String, AttributeValue>, item: T)

    fun getAllItems(): List<T>
}

class UserDao : DynamoDbDao<Users> {
    private val dynamoDbClient: DynamoDbClient = createClient()
    private val tableName = "users"
    override fun getItem(key: Map<String, AttributeValue>): Users? {
        val request = GetItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build()

        val response = dynamoDbClient.getItem(request)
        return if (response.hasItem()) {
            Users(
                userName = response.item()["user_name"]!!.s()
            )
        } else {
            null
        }
    }

    override fun putItem(item: Users) {
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


    override fun updateItem(key: Map<String, AttributeValue>, item: Users) {
        // no attribute to update
    }

    override fun getAllItems(): List<Users> {
        val scanRequest = ScanRequest.builder()
            .tableName(tableName)
            .build()
        val response = dynamoDbClient.scan(scanRequest)
        return response.items().map {
            Users(
                userName = it["user_name"]!!.s()
            )
        }
    }
}
