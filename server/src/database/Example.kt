package org.keizar.server.database


import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import javax.xml.crypto.dsig.keyinfo.KeyName

//create DynamoDbClient instance
//suspend fun createClient(): DynamoDbClient {
//
//    val awsAccessKeyId = System.getenv("AWS_ACCESS_KEY_ID")
//    val awsSecretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY")
//    val awsCreds = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)
//
//    return DynamoDbClient.builder()
//        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
//        .region(Region.EU_WEST_1)
//        .build()
//}

suspend fun scanItems(dynamoDbClient: DynamoDbClient, tableName: String) {
    val scanRequest = ScanRequest.builder()
        .tableName(tableName)
        .build()
    val response = dynamoDbClient.scan(scanRequest)
    response.items().forEach { println(it) }
}

suspend fun main() {

    val daotest = UserDao()
    val user = Users("test")
    daotest.putItem(user)

    //get all users
    val users1 = daotest.getAllItems()
    println(users1)

    daotest.deleteItem(mapOf("user_name" to AttributeValue.builder().s("test").build()))
    val users2 = daotest.getAllItems()
    println(users2)



}


