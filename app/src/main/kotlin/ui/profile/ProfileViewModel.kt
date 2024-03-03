package org.keizar.android.ui.profile

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.keizar.android.client.GameDataService
import org.keizar.android.client.SeedBankService
import org.keizar.android.client.SessionManager
import org.keizar.android.client.StreamingService
import org.keizar.android.client.UserService
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.utils.communication.LiteralChecker
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.EditUserRequest
import org.keizar.utils.communication.account.ModelConstraints
import org.keizar.utils.communication.account.User
import org.keizar.utils.communication.game.GameData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.InputStream

class ProfileViewModel : KoinComponent, AbstractViewModel() {
    private val sessionManager: SessionManager by inject()
    private val userService: UserService by inject()
    private val streamingService: StreamingService by inject()
    private val seedBankService: SeedBankService by inject()
    private val gameDataService: GameDataService by inject()
    val refresh: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Current user's information.
     */
    val self: SharedFlow<User> = sessionManager.token.mapLatest {
        userService.self()
    }.shareInBackground()

    val nickname = self.mapLatest { it.nickname }
        .localCachedStateFlow("Loading...")

    suspend fun logout() {
        sessionManager.invalidateToken()
    }

    suspend fun deleteGame(id: String) {
        gameDataService.deleteGame(id)
    }
    suspend fun removeSeed(seed: String) {
        allSeeds.value = allSeeds.value.filter { it != seed }
        seedBankService.removeSeed(seed)
    }

    suspend fun uploadAvatar(avatar: InputStream) {
        val temp = withContext(Dispatchers.IO) {
            File.createTempFile("avatar", "tmp")
        }
        try {
            avatar.use { input ->
                temp.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            streamingService.uploadAvatar(temp)
        } finally {
            temp.delete()
        }
    }

    // Change time to refresh the seeds
    val time = MutableStateFlow(0)

    val allSeeds: MutableStateFlow<List<String>> = time.map {
        seedBankService.getSeeds()
    }.localCachedStateFlow(emptyList())
    
    val allGames: MutableStateFlow<List<GameData>> = time.map {
        gameDataService.getGames()
    }.localCachedStateFlow(emptyList())

    val showNicknameEditDialog = mutableStateOf(false)

    private val _editNickname: MutableState<String> = mutableStateOf("")
    val editNickname: State<String> get() = _editNickname

    val nicknameError: MutableStateFlow<String?> = MutableStateFlow(null)
    fun showDialog() {
        showNicknameEditDialog.value = true
        refresh.value = false
    }

    suspend fun confirmDialog() {
        val success = processedUpdate()
        if (success) {
            refresh.value = true
            showNicknameEditDialog.value = false
        }
    }

    fun cancelDialog() {
        showNicknameEditDialog.value = false
        _editNickname.value = ""
    }

    private suspend fun updateNickname(): Boolean {
        val newNickname = editNickname.value
        return userService.editUser(EditUserRequest(nickname = newNickname)).success.also { success ->
            if (success) {
                nickname.value = newNickname
            }
        }
    }

    private suspend fun processedUpdate(): Boolean {
        return withTimeout(5000) {
            updateNickname()
        }
    }


    fun setEditNickname(username: String) {
        flushErrors()
        _editNickname.value = username.trim()
        val validity = LiteralChecker.checkUsername(username)
        nicknameError.value = validity.render()
    }

    private fun flushErrors() {
        nicknameError.value = null
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
}