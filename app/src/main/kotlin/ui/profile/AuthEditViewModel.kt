package org.keizar.android.ui.profile

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeout
import org.keizar.android.client.SessionManager
import org.keizar.android.client.UserService
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.ModelConstraints
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthEditViewModel(
    isPasswordEdit: Boolean,
) : AbstractViewModel(), KoinComponent {
    private val userService: UserService by inject()
    private val sessionManager: SessionManager by inject()
    private val username = sessionManager.self.value?.username
    private val nickname = sessionManager.self.value?.nickname

    val isPasswordEdit = MutableStateFlow(isPasswordEdit)

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

        val username = username
        val password = password.value

        return withTimeout(5000) {
            doAuth(username, password)
        }
    }

    private suspend fun doAuth(username: String?, newPassword: String): Boolean {

        val user =
            username?.let { userService.getUser(it) }   // TODO: 2024/3/1 Change password and login in

        return false
    }

    fun onClickSwitch() {
        flushErrors()
        if (isProcessing.value) return

        isPasswordEdit.value = !isPasswordEdit.value
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
    }
}
