package org.keizar.server.database

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest

interface DatabaseClient {
    fun <T> putItem(item: T)
    fun <T> deleteItem(key: Map<String, String>)
    fun <T> getAllItems(): List<T>
    fun <T> getItem(tableName: String, key: Map<String, AttributeValue>): T?
}

fun createClient(): DynamoDbClient {
    val awsAccessKeyId = System.getenv("AWS_ACCESS_KEY_ID")
    val awsSecretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY")
    val awsCredentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)

    return DynamoDbClient.builder()
        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
        .region(Region.EU_WEST_1)
        .build()
}

class AwsDynamoDbClient : DatabaseClient {
    private val dynamoDbClient: DynamoDbClient = createClient()
    override fun <T> getItem(tableName: String, key: Map<String, AttributeValue>): T? {
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

    override fun <T> putItem(item: T) {
        TODO("Not yet implemented")
    }

    override fun <T> deleteItem(key: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun <T> getAllItems(): List<T> {
        TODO("Not yet implemented")
    }
}

class MemoryDbClient : DatabaseClient {
    override fun <T> getItem(key: Map<String, String>): T? {
        TODO("Not yet implemented")
    }

    override fun <T> putItem(item: T) {
        TODO("Not yet implemented")
    }

    override fun <T> deleteItem(key: Map<String, String>) {
        TODO("Not yet implemented")
    }

    override fun <T> getAllItems(): List<T> {
        TODO("Not yet implemented")
    }
}