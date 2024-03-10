package org.keizar.client.services

import io.ktor.client.HttpClient
import io.ktor.client.content.LocalFileContent
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import org.keizar.client.AccessTokenProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/**
 * Services for streaming media contents, like uploading avatars.
 */
interface StreamingService : ClientService {
    /**
     * Request to update the current user's avatar to the [file].
     */
    @Throws(FileTooLargeException::class)
    suspend fun uploadSelfAvatar(file: File)
}

class FileTooLargeException() : RuntimeException()

internal class StreamingServiceImpl(
    baseUrl: String,
) : StreamingService, KoinComponent {
    private val accessTokenProvider: AccessTokenProvider by inject()
    private val baseUrl = baseUrl.removeSuffix("/")

    private val client by lazy {
        HttpClient {
            Logging {
                level = LogLevel.INFO
            }
        }
    }

    override suspend fun uploadSelfAvatar(file: File) {
        val resp = client.put("$baseUrl/users/avatar") {
            tokenHeader()
            setBody(LocalFileContent(file))
        }
        if (!resp.status.isSuccess()) {
            throw FileTooLargeException()
        }
    }

    private suspend fun HttpRequestBuilder.tokenHeader() {
        accessTokenProvider.getAccessToken()?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}