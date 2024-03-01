package org.keizar.server.modules

import aws.sdk.kotlin.runtime.auth.credentials.EnvironmentCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.FileContent
import java.io.File

interface AvatarStorage {
    suspend fun uploadAvatar(username: String, file: File, contentType: String): String
}

class InMemoryAvatarStorage : AvatarStorage {
    override suspend fun uploadAvatar(username: String, file: File, contentType: String): String = file.absolutePath
}

class AwsAvatarStorage(
    private val bucketName: String = System.getenv("AWS_AVATAR_BUCKET_NAME") ?: "keizar",
) : AvatarStorage {
    private val client = S3Client {
        region = System.getenv("AWS_REGION") ?: "us-east-1"
        credentialsProvider = EnvironmentCredentialsProvider()
    }

    override suspend fun uploadAvatar(username: String, file: File, contentType: String): String {
        @Suppress("UnnecessaryVariable", "RedundantSuppression") // IDE bug
        val filename = username
        val request = PutObjectRequest {
            bucket = bucketName
            key = "avatars/$filename"
            this.contentType = contentType
            metadata = mapOf(
                "uid" to username,
                "contentType" to contentType
            )
            body = FileContent(file)
        }

        // "https://keizar.s3.amazonaws.com/avatars/test0.png"
        client.putObject(request)

        return "https://keizar.s3.amazonaws.com/avatars/$filename"
    }
}