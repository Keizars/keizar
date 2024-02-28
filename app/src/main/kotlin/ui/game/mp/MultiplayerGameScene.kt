package org.keizar.android.ui.game.mp

import androidx.compose.foundation.layout.Column
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
import org.keizar.android.BuildConfig
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.game.BaseGamePage
import org.keizar.android.ui.game.MultiplayerGameBoardViewModel
import org.keizar.android.ui.game.mp.room.ConnectingRoomDialog
import org.keizar.client.KeizarClientFacade
import org.keizar.client.RemoteGameSession
import org.keizar.client.exception.NetworkFailureException
import org.koin.mp.KoinPlatform

private val clientFacade by KoinPlatform.getKoin().inject<KeizarClientFacade>()

@Composable
fun MultiplayerGamePage(
    roomId: UInt,
    goBack: () -> Unit,
    onClickHome: () -> Unit,
    onClickGameConfig: () -> Unit,
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
                sessionFlow.emit(
                    Result.success(
                        clientFacade.createGameSession(
                            roomId,
                            backgroundScope.scope.coroutineContext,
                            MyUserInfo
                        )
                    )
                )
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
            ConnectingRoomDialog({
                if (BuildConfig.DEBUG) {
                    Text(text = "Debug info: RemoteGameSession.player is still null")
                }
            })
        }
    }

    if (session != null && session?.getOrNull() == null) {
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
                                session?.exceptionOrNull()?.stackTraceToString()
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