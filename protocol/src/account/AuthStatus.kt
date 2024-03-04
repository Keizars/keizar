package org.keizar.utils.communication.account

enum class AuthStatus {
    SUCCESS,
    INVALID_USERNAME,
    USERNAME_TOO_LONG,
    DUPLICATED_USERNAME,
    USER_NOT_FOUND,
    WRONG_PASSWORD,
}
