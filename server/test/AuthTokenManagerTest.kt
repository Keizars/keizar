package org.keizar.server

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.keizar.server.utils.AesEncoder
import org.keizar.server.utils.AuthTokenConfig
import org.keizar.server.utils.AuthTokenManagerImpl
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days

class AuthTokenManagerTest {
    val authTokenManager = AuthTokenManagerImpl(
        config = AuthTokenConfig(
            secret = "HolgerPirk",
            expirationTime = 7.days.inWholeMilliseconds,
        ),
        encoder = AesEncoder()
    )

    @Test
    fun `can encode and decode a token`() {
        val userId = UUID.randomUUID()
        val token = authTokenManager.createToken(userId)
        assertNotNull(token)
        assertEquals(userId.toString(), authTokenManager.matchToken(token))
    }

    @Test
    fun `decode null from an invalid token`() {
        val token = "blah blah invalid token"
        assertNull(authTokenManager.matchToken(token))
    }
}