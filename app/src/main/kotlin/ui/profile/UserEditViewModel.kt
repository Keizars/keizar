package org.keizar.android.ui.profile

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeout
import org.keizar.android.client.UserService
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.ChangePasswordRequest
import org.keizar.utils.communication.account.ModelConstraints
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserEditViewModel(
) : AbstractViewModel(), KoinComponent {
    private val userService: UserService by inject()

    private val _password: MutableState<String> = mutableStateOf("")
    val password: State<String> get() = _password

    private val _verifyPassword: MutableState<String> = mutableStateOf("")
    val verifyPassword: State<String> get() = _verifyPassword

    private val nicknameValid: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val passwordError: MutableStateFlow<String?> = MutableStateFlow(null)
    val verifyPasswordError: MutableStateFlow<String?> = MutableStateFlow(null)


    val isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun setPassword(password: String) {
        flushErrors()
        _password.value = password
        passwordError.value = null
    }


    fun setVerifyPassword(password: String) {
        flushErrors()
        _verifyPassword.value = password
        verifyPasswordError.value = null
    }

    suspend fun processedUpdate(): Boolean {
        if (!checkInputs()) return false

        val password = password.value

        return withTimeout(5000) {
            doPasswordUpdate(password)
        }
    }

    private suspend fun doPasswordUpdate(newPassword: String): Boolean {
        return userService.changePassword(ChangePasswordRequest(newPassword)).success
    }

    private fun checkInputs(): Boolean {
        val password = password.value
        if (password.isEmpty()) {
            passwordError.value = "Please enter password"
            return false
        }
        val verifyPassword = verifyPassword.value
        if (verifyPassword.isEmpty()) {
            verifyPasswordError.value = "Please re-enter your password"
            return false
        }
        if (password != verifyPassword) {
            verifyPasswordError.value = "Passwords do not match. Please re-enter your password"
            return false
        }
        return true
    }

    private fun flushErrors() {
        passwordError.value = null
        nicknameValid.value = false
    }
}

private fun AuthStatus.render(): String? {
    return when (this) {
        AuthStatus.INVALID_USERNAME -> "Must consist of English characters, digits, '-' or '_'"
        AuthStatus.USERNAME_TOO_LONG -> "Nickname is too long. Maximum length is ${ModelConstraints.USERNAME_MAX_LENGTH} characters"
        AuthStatus.DUPLICATED_USERNAME -> "Nickname is already taken. Please pick another one"
        AuthStatus.SUCCESS -> null
        AuthStatus.USER_NOT_FOUND -> "User not found"
        AuthStatus.WRONG_PASSWORD -> "Wrong password"
        else -> {""}
    }
}
