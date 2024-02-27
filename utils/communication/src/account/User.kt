package org.keizar.utils.communication.account

import kotlinx.serialization.Serializable

@Serializable
class User(
    val nickname: String,
    val username: String,
    val avatarUrl: String,
)