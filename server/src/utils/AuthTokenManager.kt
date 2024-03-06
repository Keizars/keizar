package org.keizar.server.utils

import java.util.*

/**
 * A manager for creating and validating authentication tokens.
 * Created in and accessible from the [ServerContext].
 */
interface AuthTokenManager {
    /**
     * Creates an authentication token for the given user ID.
     */
    fun createToken(userId: String): String

    /**
     * Check the validity of the given token and return the user ID if it is valid.
     */
    fun matchToken(token: String): String?
}

/**
 * An implementation of the [AuthTokenManager] interface that uses a given [Encoder] to
 * encrypt the username and the expiration time into the token.
 *
 * @param config The configuration for the authentication token manager. It contains the token secret and the expiration time.
 * @param encoder The [Encoder] used to encrypt and decrypt the String.
 */
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

/**
 * A helper extension function to convert a String to a UUID or return null if it is not a valid UUID.
 */
fun String.formatToUuidOrNull(): UUID? {
    return try {
        UUID.fromString(this)
    } catch (e: Exception) {
        null
    }
}

/**
 * A configuration class for the [AuthTokenManager].
 *
 * @param secret The secret used to encrypt and decrypt the token.
 * @param expirationTime The time in milliseconds after which the token will expire. If it is set to -1, the token will never expire.
 */
class AuthTokenConfig(
    val secret: ByteArray,
    val expirationTime: Long,
)
