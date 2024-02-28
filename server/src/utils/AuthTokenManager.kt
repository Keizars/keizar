package org.keizar.server.utils

import java.util.*

interface AuthTokenManager {
    fun createToken(userId: String): String
    fun matchToken(token: String): String?
}

class AuthTokenManagerImpl(
    private val config: AuthTokenConfig,
    private val encoder: Encoder,
) : AuthTokenManager {
    override fun createToken(userId: String): String {
        val validUntil = if (config.expirationTime == (-1).toLong()) {
            "never"
        } else {
            (System.currentTimeMillis() + config.expirationTime).toString()
        }
        return encoder.encode("$userId $validUntil", config.secret)
    }

    override fun matchToken(token: String): String? {
        return try {
            val decodedString = encoder.decode(token, config.secret)
            if (!decodedString.contains(" ")) return null
            val (userId, validUntil) = decodedString.split(" ")
            if (validUntil != "never" && validUntil.toLong() < System.currentTimeMillis()) return null
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
