package org.keizar.android.ui.profile

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.keizar.android.data.GameStartConfigurationEncoder
import org.keizar.android.data.SessionManager
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.client.services.GameDataService
import org.keizar.client.services.SeedBankService
import org.keizar.client.services.StreamingService
import org.keizar.client.services.UserService
import org.keizar.game.BoardProperties
import org.keizar.utils.communication.LiteralChecker
import org.keizar.utils.communication.account.AuthStatus
import org.keizar.utils.communication.account.EditUserRequest
import org.keizar.utils.communication.account.ModelConstraints
import org.keizar.utils.communication.account.User
import org.keizar.utils.communication.game.GameDataGet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.InputStream

@Immutable
data class SavedSeed(
    val configurationSeed: String,
    val layoutSeed: Int = GameStartConfigurationEncoder.decode(configurationSeed)?.layoutSeed ?: 0,
    val boardProperties: BoardProperties = BoardProperties.getStandardProperties(layoutSeed)
)

class ProfileViewModel : KoinComponent, AbstractViewModel() {
    private val sessionManager: SessionManager by inject()
    private val userService: UserService by inject()
    private val streamingService: StreamingService by inject()
    private val seedBankService: SeedBankService by inject()
    private val gameDataService: GameDataService by inject()

    /**
     * Current user's information.
     */
    val self: SharedFlow<User> = sessionManager.token.mapLatest {
        userService.self()
    }.shareInBackground()

    val nickname = self.mapLatest { it.nickname }
        .localCachedStateFlow("Loading...")

    suspend fun getAvatarUrl(opponentUserName: String): String {
        return userService.getUser(opponentUserName).avatarUrlOrDefault()
    }

    suspend fun logout() {
        sessionManager.invalidateToken()
    }


    /**
     * Deletes a saved game from the server. If the request fails, the game is not removed from the local cache.
     */
    suspend fun deleteGame(id: String) {
        try {
            gameDataService.deleteGame(id)
        } catch (error: Throwable) {
            return
        }
        allGames.value = allGames.value.filter { it.dataId != id }
    }

    /**
     * Deletes a saved board from the server. If the request fails, the board is not removed from the local cache.
     */
    suspend fun removeSeed(seed: String) {
        try {
            seedBankService.removeSeed(seed)
        } catch (error: Throwable) {
            return
        }
        allSeeds.value = allSeeds.value.filter { it.configurationSeed != seed }
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
            streamingService.uploadSelfAvatar(temp)
        } finally {
            temp.delete()
        }
    }

    /**
     *  A dummy flow to trigger refreshes of saved seeds and games
     */
    val time = MutableStateFlow(0)

    /**
     * a flag to indicate if the seeds are being loaded, used in the UI to show a loading spinner
     */
    val isLoadingSeeds = MutableStateFlow(true)

    /**
     * A flow of all the saved seeds of the user
     */
    val allSeeds: MutableStateFlow<List<SavedSeed>> = time.map {
        seedBankService.getSeeds()
    }.onStart { isLoadingSeeds.value = true }.map { seeds ->
        seeds.map { SavedSeed(it) }
    }.onEach {
        isLoadingSeeds.value = false
    }.localCachedStateFlow(emptyList())

    /**
     * The selected seed to be used to display the board layout when the user selects a saved board
     */
    val selectedSeed: MutableStateFlow<SavedSeed?> = MutableStateFlow(null)

    /**
     * The selected game to be used to display the game details when the user selects a saved game
     */
    val selectedGame: MutableStateFlow<GameDataGet?> = MutableStateFlow(null)

    /**
     * a flag to indicate if the games are being loaded, used in the UI to show a loading spinner
     */
    var isLoadingGames = MutableStateFlow(true)

    /**
     * A flow of all the saved games of the user
     */
    val allGames: MutableStateFlow<List<GameDataGet>> = time.map {
        gameDataService.getGames()
    }.onStart { isLoadingGames.value = true }.onEach {
        isLoadingGames.value = false
    }.localCachedStateFlow(emptyList())


    val showNicknameEditDialog = mutableStateOf(false)

    private val _editNickname: MutableState<String> = mutableStateOf("")
    val editNickname: State<String> get() = _editNickname

    val nicknameError: MutableStateFlow<String?> = MutableStateFlow(null)
    fun showDialog() {
        showNicknameEditDialog.value = true
    }

    suspend fun confirmDialog() {
        val success = processedUpdate()
        if (success) {
            showNicknameEditDialog.value = false
            _editNickname.value = _editNickname.value.trim()
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


    fun setEditNickname(nickName: String) {
        flushErrors()
        _editNickname.value = nickName
        val validity = LiteralChecker.checkNickname(nickName)
        nicknameError.value = validity.render()
    }

    private fun flushErrors() {
        nicknameError.value = null
    }

    private fun AuthStatus.render(): String? {
        return when (this) {
            AuthStatus.INVALID_USERNAME -> "Must consist of English characters, digits, '-' or '_'"
            AuthStatus.USERNAME_TOO_LONG -> "Username is too long. Maximum length is ${ModelConstraints.USERNAME_MAX_LENGTH} characters"
            AuthStatus.DUPLICATED_USERNAME -> "Username is already taken. Please pick another one"
            AuthStatus.SUCCESS -> null
            AuthStatus.USER_NOT_FOUND -> "User not found"
            AuthStatus.WRONG_PASSWORD -> "Wrong password"
            AuthStatus.NICKNAME_TOO_LONG -> "Nickname is too long. Maximum length is ${ModelConstraints.USERNAME_MAX_LENGTH} characters"
            AuthStatus.INVALID_NICKNAME -> "The nickname cannot start with space:' '"
        }
    }
}