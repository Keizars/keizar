package org.keizar.server.utils

import java.util.*

interface AuthTokenManager {
    fun createToken(userId: UUID): String
    fun matchToken(token: String): String?
}

class AuthTokenManagerImpl(
    private val config: AuthTokenConfig,
    private val encoder: Encoder,
): AuthTokenManager {
    override fun createToken(userId: UUID): String {
        val validUntil = System.currentTimeMillis() + config.expirationTime
        return encoder.encode("$userId $validUntil", config.secret)
    }

    override fun matchToken(token: String): String? {
        return try {
            val decodedString = encoder.decode(token, config.secret)
            if (!decodedString.contains(" ")) return null
            val (userId, validUntil) = decodedString.split(" ")
            if (validUntil.toLong() < System.currentTimeMillis()) return null
            return userId
        } catch (e: Exception) {
            null
        }
    }
}

fun String.formatToUuidOrNull(): UUID? {
    return try {
        UUID.fromString(this)
    } catch (e: Exception) {
        null
    }
}

class AuthTokenConfig(
    val secret: String,
    val expirationTime: Long
)
