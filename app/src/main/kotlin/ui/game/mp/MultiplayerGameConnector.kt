package org.keizar.android.ui.game.mp

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.yield
import me.him188.ani.utils.logging.error
import me.him188.ani.utils.logging.info
import me.him188.ani.utils.logging.warn
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.ErrorMessage
import org.keizar.client.Room
import org.keizar.client.exceptions.RoomFullException
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

    val connectionError: MutableStateFlow<ConnectionError?> = MutableStateFlow(null)
    val error: MutableStateFlow<ErrorMessage?> = MutableStateFlow(null)

    val client: SharedFlow<Room> = flow {
        while (true) {
            try {
                logger.info { "roomService.connect: Connecting" }
                val room = roomService.connect(roomId, backgroundScope.coroutineContext)
                error.value = null
                emit(room)
                logger.info { "roomService.connect: successfully" }

                // Wait until session closes
                select { room.onComplete {} }
                if (!currentCoroutineContext().isActive) {
                    logger.warn { "Room connection closed, current coroutine is not active, returning" }
                    return@flow
                }
                logger.warn { "Room connection closed, current coroutine is active, attempting to reconnect after 2 seconds" }
                yield() // check cancellation
                error.value = ErrorMessage.networkErrorRecovering()
                delay(2.seconds)
            } catch (e: RoomFullException) {
                connectionError.value = ConnectionError.NetworkError(e)
                logger.error(e) { "roomService.connect failed: RoomFullException" }
                delay(3.seconds)
                continue
            } catch (e: Exception) {
                logger.error(e) { "roomService.connect failed" }
                error.value = ErrorMessage.networkErrorRecovering()
                delay(3.seconds)
                continue
            }
        }
    }.shareInBackground(SharingStarted.Eagerly)

    val session = client.mapLatest {
        logger.info { "Getting GameSession" }
        val session = try {
            it.getGameSession()
        } catch (e: Throwable) {
            logger.error(e) { "Failed to get GameSession" }
            error.value = ErrorMessage.networkErrorRecovering(e)
            null
        }
        logger.info { "Got GameSession: $session" }
        session
    }.shareInBackground(SharingStarted.Eagerly)

    val selfPlayer = client.map { it.selfPlayer }
}
