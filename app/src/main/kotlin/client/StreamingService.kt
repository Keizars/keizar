package org.keizar.android.client

import io.ktor.client.HttpClient
import io.ktor.client.content.LocalFileContent
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.first
import org.keizar.android.BuildConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

interface StreamingService {
    suspend fun uploadAvatar(file: File)
}

internal class StreamingServiceImpl(
    baseUrl: String = BuildConfig.SERVER_ENDPOINT
) : StreamingService, KoinComponent {
    private val sessionManager by inject<SessionManager>()


    private val baseUrl = baseUrl.removeSuffix("/")

    private val client by lazy {
        HttpClient {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(
                            accessToken = sessionManager.token.first() ?: "",
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

    override suspend fun uploadAvatar(file: File) {
        client.put("$baseUrl/users/avatar") {
            setBody(LocalFileContent(file))
        }
    }
}