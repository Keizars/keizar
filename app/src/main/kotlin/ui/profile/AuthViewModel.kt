package org.keizar.android.ui.profile

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.keizar.android.client.SessionManager
import org.keizar.android.client.UserService
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.utils.communication.LiteralChecker
import org.keizar.utils.communication.account.AuthRequest
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.ModelConstraints
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthViewModel(
    isRegister: Boolean,
) : AbstractViewModel(), KoinComponent {
    private val userService: UserService by inject()
    private val sessionManager: SessionManager by inject()

    val isRegister = MutableStateFlow(isRegister)

    private val _username: MutableState<String> = mutableStateOf("")
    val username: State<String> get() = _username

    private val _password: MutableState<String> = mutableStateOf("")
    val password: State<String> get() = _password

    private val _verifyPassword: MutableState<String> = mutableStateOf("")
    val verifyPassword: State<String> get() = _verifyPassword

    val usernameError: MutableStateFlow<String?> = MutableStateFlow(null)
    val usernameValid: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val passwordError: MutableStateFlow<String?> = MutableStateFlow(null)
    val verifyPasswordError: MutableStateFlow<String?> = MutableStateFlow(null)


    val isProcessing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val agreementChecked: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun setUsername(username: String) {
        flushErrors()
        _username.value = username.trim()
        val validity = LiteralChecker.checkUsername(username)
        usernameError.value = validity.render()
    }

    suspend fun checkUsernameValidity() {
        if (isRegister.value) {
            val response = userService.isAvailable(username.value)
            if (!response.validity) {
                usernameError.value = AuthStatus.DUPLICATED_USERNAME.render()
            } else {
                usernameValid.value = true
            }
        }
    }

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

    suspend fun proceedLogin(): Boolean {
        if (!checkInputs()) return false

        val username = username.value
        val password = password.value

        return doAuth(username, password, isRegister.value)
    }

    private suspend fun doAuth(username: String, password: String, isRegister: Boolean): Boolean {

        val response = if (isRegister) {
            userService.register(AuthRequest(username, password))
        } else {
            userService.login(AuthRequest(username, password))
        }
        when (response.status) {
            AuthStatus.SUCCESS -> {
                return if (isRegister) {
                    // register OK, then log in
                    doAuth(username, password, isRegister = false)
                } else {
                    sessionManager.setToken(response.token!!)
                    //                    LocalSessionToken.value = response.token
                    //                    History.navigate { authReturnOrHome() }
                    true
                }
            }

            AuthStatus.INVALID_USERNAME,
            AuthStatus.USERNAME_TOO_LONG,
            AuthStatus.DUPLICATED_USERNAME,
            AuthStatus.USER_NOT_FOUND -> {
                usernameError.value = response.status.render()
            }

            AuthStatus.WRONG_PASSWORD -> {
                passwordError.value = response.status.render()
            }
        }
        return false
    }

    private fun checkInputs(): Boolean {
        val username = username.value
        if (username.isEmpty()) {
            usernameError.value = "Please enter username"
            return false
        }
        val password = password.value
        if (password.isEmpty()) {
            passwordError.value = "Please enter password"
            return false
        }
        val verifyPassword = verifyPassword.value
        if (verifyPassword.isEmpty() && isRegister.value) {
            verifyPasswordError.value = "Please re-enter your password"
            return false
        }
        if (password != verifyPassword && isRegister.value) {
            verifyPasswordError.value = "Passwords do not match. Please re-enter your password"
            return false
        }
        return true
    }

    fun onClickSwitch() {
        flush()
        if (isProcessing.value) return

        isRegister.value = !isRegister.value
    }

    private fun flush() {
        _username.value = ""
        _password.value = ""
        _verifyPassword.value = ""
        flushErrors()
    }

    private fun flushErrors() {
        usernameError.value = null
        passwordError.value = null
        usernameValid.value = false
    }
}

private fun AuthStatus.render(): String? {
    return when (this) {
        AuthStatus.INVALID_USERNAME -> "Must consist of English characters, digits, '-' or '_'"
        AuthStatus.USERNAME_TOO_LONG -> "Username is too long. Maximum length is ${ModelConstraints.USERNAME_MAX_LENGTH} characters"
        AuthStatus.DUPLICATED_USERNAME -> "Username is already taken. Please pick another one"
        AuthStatus.SUCCESS -> null
        AuthStatus.USER_NOT_FOUND -> "User not found"
        AuthStatus.WRONG_PASSWORD -> "Wrong password"
    }
}
