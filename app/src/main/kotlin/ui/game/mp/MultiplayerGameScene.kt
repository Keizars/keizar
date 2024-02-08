package org.keizar.android.ui.game.mp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.keizar.android.ui.external.placeholder.placeholder
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.game.BaseGamePage
import org.keizar.android.ui.game.rememberGameBoardViewModel
import org.keizar.client.GameRoomClient
import org.keizar.client.RemoteGameSession
import org.keizar.client.exception.NetworkFailureException

@Composable
fun MultiplayerGamePage(
    roomId: UInt,
    goBack: () -> Unit,
    onClickHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundScope = remember { CoroutineScopeOwner() }
    val sessionFlow = remember {
        MutableStateFlow<Result<RemoteGameSession>?>(null)
    }
    val session by sessionFlow.collectAsStateWithLifecycle()

    if (session == null) {
        ConnectingRoomDialog()
    }

    DisposableEffect(roomId) {
        val job = backgroundScope.scope.launch {
            try {
                GameRoomClient.create().use { client ->
                    sessionFlow.emit(
                        Result.success(
                            RemoteGameSession.createAndConnect(
                                client.getRoom(roomId),
                                backgroundScope.scope.coroutineContext
                            )
                        )
                    )
                }
            } catch (e: Throwable) {
                sessionFlow.emit(Result.failure(e))
                throw e
            }
        }

        onDispose {
            job.cancel()
            session?.getOrNull()?.close()
        }
    }

    if (session?.isFailure == true) {
        AlertDialog(
            onDismissRequest = goBack,
            confirmButton = {
                TextButton(onClick = goBack) {
                    Text(text = "OK")
                }
            },
            text = {
                if (session?.exceptionOrNull() is NetworkFailureException) {
                    Text(text = "Failed to join the room. Please check your network connection.")
                } else {
                    Text(text = "Failed to join the room. Please check the room id.")
                }
            },
        )
    }

    session?.getOrNull()?.let {
        BaseGamePage(
            rememberGameBoardViewModel(session = it, selfPlayer = it.player),
            onClickHome = onClickHome,
            onClickGameConfig = { },
            modifier
        )
    } ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .placeholder(true)
        )
    }
}

class CoroutineScopeOwner : RememberObserver, AbstractViewModel() {
    val scope get() = backgroundScope
}