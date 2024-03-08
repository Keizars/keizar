package org.keizar.server.data

import aws.sdk.kotlin.runtime.auth.credentials.EnvironmentCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.FileContent
import java.io.File

interface AvatarStorage {
    suspend fun uploadAvatar(username: String, file: File, filename: String, contentType: String): String
}

class InMemoryAvatarStorage : AvatarStorage {
    override suspend fun uploadAvatar(username: String, file: File, filename: String, contentType: String): String =
        file.absolutePath
}

class AwsAvatarStorage(
    private val bucketName: String = System.getenv("AWS_AVATAR_BUCKET_NAME") ?: "keizar",
) : AvatarStorage {
    private val client = S3Client {
        region = System.getenv("AWS_REGION") ?: "us-east-1"
        credentialsProvider = EnvironmentCredentialsProvider()
    }

    override suspend fun uploadAvatar(username: String, file: File, filename: String, contentType: String): String {
        val request = PutObjectRequest {
            bucket = bucketName
            key = "avatars/$filename"
            this.contentType = contentType
            metadata = mapOf(
                "uid" to username,
                "contentType" to contentType
            )
            body = FileContent(file)
//            checksumAlgorithm = ChecksumAlgorithm.Crc32
//            checksumCrc32 = file.inputStream().use { it.readBytes().contentToString().hashCode().toLong() }
        }

        // "https://keizar.s3.amazonaws.com/avatars/test0.png"
        client.putObject(request)

        return "https://keizar.s3.amazonaws.com/avatars/$filename"
    }
}