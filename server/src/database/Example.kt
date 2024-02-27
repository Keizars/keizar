package org.keizar.server.database


import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider

//create DynamoDbClient instance
suspend fun createClient(region: Region): DynamoDbClient {

    val awsAccessKeyId = System.getenv("AWS_ACCESS_KEY_ID")
    val awsSecretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY")
    val awsCreds = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)

    return DynamoDbClient.builder()
        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        .region(region)
        .build()
}

suspend fun scanItems(dynamoDbClient: DynamoDbClient, tableName: String) {
    val scanRequest = ScanRequest.builder()
        .tableName(tableName)
        .build()
    val response = dynamoDbClient.scan(scanRequest)
    response.items().forEach { println(it) }
}

suspend fun main() {
    val region = Region.EU_WEST_1 // Don't change this to avoid fail to connect to DB
    val dbClient = createClient(region)
    val tables = dbClient.listTables().tableNames()

    println(tables)

}


