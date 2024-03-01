package org.keizar.android.ui.game.mp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import me.him188.ani.utils.logging.error
import org.keizar.android.BuildConfig
import org.keizar.android.client.SessionManager
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.game.BaseGamePage
import org.keizar.android.ui.game.GameBoard
import org.keizar.android.ui.game.MultiplayerGameBoardViewModel
import org.keizar.android.ui.game.mp.room.ConnectingRoomDialog
import org.keizar.android.ui.game.transition.CapturedPiecesHost
import org.keizar.client.GameRoomClient
import org.keizar.client.KeizarWebsocketClientFacade
import org.keizar.client.exception.RoomFullException
import org.keizar.game.GameSession
import org.keizar.utils.communication.game.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds


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
) : AbstractViewModel(), KoinComponent {
    private val sessionManager: SessionManager by inject()
    private val clientFacade: KeizarWebsocketClientFacade by inject()

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

    val selfPlayer = client.map { it.selfPlayer }

//    val selfPlayer = combine(client, sessionManager.self) { client, self ->
//        client.players.firstOrNull { it.username == self?.username }
//    }.shareInBackground()
}

@Composable
fun MultiplayerGameScene(
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
            title = { Text(text = "Error") },
            text = {
                Column {
                    if (error is ConnectionError.NetworkError) {
                        Text(text = "Failed to join the room. Please check your internet connection.")
                    } else {
                        Text(text = "Failed to join the room. Please check the room id.")
                    }

                    if (BuildConfig.DEBUG) {
                        Text(
                            text = "Debug info: \n${
                                error?.toDebugString()
                            }"
                        )
                    }
                }
            },
        )
    }

    val session by connector.session.collectAsStateWithLifecycle(null)

    session?.let { s ->
        val vm = remember {
            MultiplayerGameBoardViewModel(s, s.player)
        }
        MultiplayerGamePage(vm, onClickHome, onClickGameConfig, modifier)
    } ?: run {
        ConnectingRoomDialog(extra = {
            if (BuildConfig.DEBUG) {
                Text(text = "Debug info: RemoteGameSession.player is still null")
            }
        })
    }
}

@Composable
private fun MultiplayerGamePage(
    vm: MultiplayerGameBoardViewModel,
    onClickHome: () -> Unit,
    onClickGameConfig: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BaseGamePage(
        vm,
        onClickHome = onClickHome,
        onClickGameConfig = onClickGameConfig,
        modifier = modifier,
        board = { size ->
            GameBoard(
                vm = vm,
                Modifier
                    .padding(vertical = 16.dp)
                    .size(size),
                opponentCapturedPieces = { tileSize, sourceCoordinates ->
//                        AsyncImage(
//                            null,
//                            
//                        )
                    val opponentName by vm.opponentName.collectAsState("Opponent")
                    Text(text = opponentName, style = MaterialTheme.typography.bodyMedium)

                    CapturedPiecesHost(
                        capturedPieceHostState = vm.theirCapturedPieceHostState,
                        slotSize = tileSize,
                        sourceCoordinates = sourceCoordinates,
                    )
                },
            )
        }
    )
}

class CoroutineScopeOwner : RememberObserver, AbstractViewModel() {
    val scope get() = backgroundScope
}

@Preview
@Composable
private fun PreviewMultiplayerGame() {
    val vm = remember {
        MultiplayerGameBoardViewModel(GameSession.create(), Player.FirstWhitePlayer)
    }
    ProvideCompositionalLocalsForPreview {
        MultiplayerGamePage(
            vm,
            onClickHome = {},
            onClickGameConfig = {},
        )
    }
}