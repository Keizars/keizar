package org.keizar.server

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.keizar.server.utils.AesEncoder
import org.keizar.server.utils.AuthTokenConfig
import org.keizar.server.utils.AuthTokenManagerImpl
import java.util.UUID
import kotlin.test.assertEquals

class AuthTokenManagerTest {

    @Test
    fun `can encode and decode a token`() {
        val authTokenManager = AuthTokenManagerImpl(
            config = AuthTokenConfig(
                secret = "HolgerPirk",
                expirationTime = -1,
            ),
            encoder = AesEncoder()
        )
        val userId = UUID.randomUUID()
        val token = authTokenManager.createToken(userId.toString())
        assertNotNull(token)
        assertEquals(userId.toString(), authTokenManager.matchToken(token))
    }

    @Test
    fun `decode null from an invalid token`() {
        val authTokenManager = AuthTokenManagerImpl(
            config = AuthTokenConfig(
                secret = "HolgerPirk",
                expirationTime = -1,
            ),
            encoder = AesEncoder()
        )
        val token = "blah blah invalid token"
        assertNull(authTokenManager.matchToken(token))
    }

    @Test
    fun `decode null from an expired token`() {
        val authTokenManager = AuthTokenManagerImpl(
            config = AuthTokenConfig(
                secret = "HolgerPirk",
                expirationTime = 1,
            ),
            encoder = AesEncoder()
        )
        val userId = UUID.randomUUID()
        val token = authTokenManager.createToken(userId.toString())
        assertNotNull(token)

        Thread.sleep(200)
        assertNull(authTokenManager.matchToken(token))
    }
}