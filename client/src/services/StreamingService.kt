package org.keizar.client.services

import io.ktor.client.HttpClient
import io.ktor.client.content.LocalFileContent
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.put
import io.ktor.client.request.setBody
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
    suspend fun uploadSelfAvatar(file: File)
}

internal class StreamingServiceImpl(
    baseUrl: String,
) : StreamingService, KoinComponent {
    private val accessTokenProvider: AccessTokenProvider by inject()
    private val baseUrl = baseUrl.removeSuffix("/")

    private val client by lazy {
        HttpClient {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(
                            accessToken = accessTokenProvider.getAccessToken() ?: "",
                            refreshToken = ""
                        )
                    }
                }
            }
            Logging {
                level = LogLevel.INFO
            }
        }
    }

    override suspend fun uploadSelfAvatar(file: File) {
        client.put("$baseUrl/users/avatar") {
            setBody(LocalFileContent(file))
        }
    }
}