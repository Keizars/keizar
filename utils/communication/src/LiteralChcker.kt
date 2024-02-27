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

        fun checkCourseCode(code: String): Boolean {
            return code.length <= ModelConstraints.COURSE_CODE_MAX_LENGTH
                    && check(code, ModelConstraints.CODE_REGEX)
        }

        fun checkArticleCode(code: String): Boolean {
            return code.length <= ModelConstraints.ARTICLE_CODE_MAX_LENGTH
                    && check(code, ModelConstraints.CODE_REGEX)
        }

        fun checkQuestionCode(code: String): Boolean {
            return code.length <= ModelConstraints.QUESTION_CODE_MAX_LENGTH
                    && check(code, ModelConstraints.CODE_REGEX)
        }
    }
}