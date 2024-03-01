package org.keizar.android.ui.game.mp

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import me.him188.ani.utils.logging.error
import org.keizar.android.BuildConfig
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.game.BaseGamePage
import org.keizar.android.ui.game.MultiplayerGameBoardViewModel
import org.keizar.android.ui.game.mp.room.ConnectingRoomDialog
import org.keizar.client.GameRoomClient
import org.keizar.client.KeizarWebsocketClientFacade
import org.keizar.client.exception.RoomFullException
import org.koin.mp.KoinPlatform
import kotlin.time.Duration.Companion.seconds

private val clientFacade by KoinPlatform.getKoin().inject<KeizarWebsocketClientFacade>()

private sealed class ConnectionError(
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
private class MultiplayerGameConnector(
    roomId: UInt
) : AbstractViewModel() {
    val error: MutableStateFlow<ConnectionError?> = MutableStateFlow(null)

    val client: SharedFlow<GameRoomClient> = flow {
        while (true) {
            emit(
                try {
                    clientFacade.connect(roomId, backgroundScope.coroutineContext)
                } catch (e: RoomFullException) {
                    error.value = ConnectionError.NetworkError(e)
                    continue
                } catch (e: Exception) {
                    logger.error(e) { "Failed to get self" }
                    delay(5.seconds)
                    continue
                }
            )
            return@flow
        }
    }.shareInBackground()

    val session = client.mapLatest {
        it.getGameSession()
    }.shareInBackground()
}

@Composable
fun MultiplayerGamePage(
    roomId: UInt,
    goBack: () -> Unit,
    onClickHome: () -> Unit,
    onClickGameConfig: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val connector = remember(roomId) {
        MultiplayerGameConnector(roomId)
    }

    val error by connector.error.collectAsStateWithLifecycle()
    if (error != null) {
        AlertDialog(
            onDismissRequest = goBack,
            confirmButton = {
                TextButton(onClick = goBack) {
                    Text(text = "OK")
                }
            },
            text = {
                if (error is ConnectionError.NetworkError) {
                    Text(text = "Failed to join the room. Please check your network connection.")
                } else {
                    Text(text = "Failed to join the room. Please check the room id.")
                }
            },
        )
    }

    val client by connector.client.collectAsStateWithLifecycle(null)
    val session by connector.session.collectAsStateWithLifecycle(null)

    client?.let {
        val player by it.player.collectAsStateWithLifecycle(initialValue = null)
        player?.let { p ->
            BaseGamePage(
                remember {
                    MultiplayerGameBoardViewModel(it, p)
                },
                onClickHome = onClickHome,
                onClickGameConfig = onClickGameConfig,
                modifier = modifier
            )
        } ?: run {
            ConnectingRoomDialog(extra = {
                if (BuildConfig.DEBUG) {
                    Text(text = "Debug info: RemoteGameSession.player is still null")
                }
            })
        }
    }

    if (session == null) {
        AlertDialog(
            onDismissRequest = goBack,
            confirmButton = { Text(text = "OK") },
            title = { Text(text = "Error") },
            text = {
                Column {
                    Text(text = "Failed to join the room. Please check your internet connection and try again.")

                    if (BuildConfig.DEBUG) {
                        Text(
                            text = "Debug info: \n${
                                error?.toDebugString()
                            }"
                        )
                    }
                }
            }
        )
    }
}

class CoroutineScopeOwner : RememberObserver, AbstractViewModel() {
    val scope get() = backgroundScope
}