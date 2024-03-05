package org.keizar.utils.communication

import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.ModelConstraints

class LiteralChecker {
    companion object {
        fun check(string: String, regexStr: String): Boolean {
            return Regex(regexStr).matches(string)
        }

        fun checkUsername(username: String): AuthStatus {
            if (username.length > ModelConstraints.USERNAME_MAX_LENGTH) {
                return AuthStatus.USERNAME_TOO_LONG
            }
            val valid = Regex(ModelConstraints.USERNAME_REGEX).matches(username)
            if (!valid) {
                return AuthStatus.INVALID_USERNAME
            }
            return AuthStatus.SUCCESS
        }

        fun checkNickname(nickname: String): AuthStatus {
            if (nickname.length > ModelConstraints.USERNAME_MAX_LENGTH) {
                return AuthStatus.NICKNAME_TOO_LONG
            }
            val valid = Regex(ModelConstraints.NICKNAME_REGEX).matches(nickname)
            if (!valid) {
                return AuthStatus.INVALID_NICKNAME
            }
            return AuthStatus.SUCCESS
        }
    }
}