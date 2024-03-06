package org.keizar.android.ui.game.mp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.client.Room
import org.keizar.client.exception.RoomFullException
import org.keizar.client.services.RoomService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds


sealed class ConnectionError(
    val exception: Exception? = null
) {
    class NetworkError(
        exception: Exception? = null
    ) : ConnectionError(exception)

    fun toDebugString(): String? {
        return exception?.stackTraceToString()
    }
}

/**
 * Handles connection to the game
 */
class MultiplayerGameConnector(
    roomId: UInt
) : AbstractViewModel(), KoinComponent {
    private val roomService: RoomService by inject()

    val error: MutableStateFlow<ConnectionError?> = MutableStateFlow(null)

    val client: SharedFlow<Room> = flow {
        while (true) {
            try {
                emit(roomService.connect(roomId, backgroundScope.coroutineContext))
                logger.info { "Successfully connected to room" }
            } catch (e: RoomFullException) {
                error.value = ConnectionError.NetworkError(e)
                logger.error(e) { "Failed to connect to room: RoomFullException" }
                delay(5.seconds)
                continue
            } catch (e: Exception) {
                logger.error(e) { "Failed to get self" }
                delay(5.seconds)
                continue
            }
            return@flow
        }
    }.shareInBackground(SharingStarted.Eagerly)

    val session = client.mapLatest {
        logger.info { "Connecting GameSession" }
        val session = it.getGameSession()
        logger.info { "Connected to GameSession: $session" }
        session
    }.shareInBackground(SharingStarted.Eagerly)

    val selfPlayer = client.map { it.selfPlayer }
}
