package org.keizar.utils.communication.account

import kotlinx.serialization.Serializable

/**
 * Request to edit user information.
 *
 * Pass properties as not `null` to edit them.
 */
@Serializable
data class EditUserRequest(
    val username: String? = null,
    val nickname: String? = null,
)

@Serializable
data class EditUserResponse(
    val success: Boolean,
)

/**
 * Request to change user password.
 */
@Serializable
data class ChangePasswordRequest(
    val password: String,
)

@Serializable
data class ChangePasswordResponse(
    val success: Boolean,
)