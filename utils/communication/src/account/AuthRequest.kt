package org.keizar.utils.communication.account

import kotlinx.serialization.Serializable


@Serializable
class AuthRequest(
    val username: String,
    val password: String,
)

@Serializable
class AuthResponse(
    val status: AuthStatus,
    val token: String? = null,
)

@Serializable
class UsernameValidityResponse(
    val validity: Boolean
)
