package org.keizar.utils.communication.account

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val nickname: String,
    val username: String,
    val avatarUrl: String,
)