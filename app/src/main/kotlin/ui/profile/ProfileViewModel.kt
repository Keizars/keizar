package org.keizar.android.ui.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import org.keizar.android.client.SeedBankService
import org.keizar.android.client.SessionManager
import org.keizar.android.client.StreamingService
import org.keizar.android.client.UserService
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.utils.communication.account.User
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.InputStream

class ProfileViewModel : KoinComponent, AbstractViewModel() {
    private val sessionManager: SessionManager by inject()
    private val userService: UserService by inject()
    private val streamingService: StreamingService by inject()
    private val seedBankService: SeedBankService by inject()

    /**
     * Current user's information.
     */
    val self: SharedFlow<User> = sessionManager.token.mapLatest {
        userService.self()
    }.shareInBackground()

    suspend fun logout() {
        sessionManager.invalidateToken()
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
}