package org.keizar.android.ui.game.mp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.keizar.android.ui.external.placeholder.placeholder
import org.keizar.android.ui.game.BaseGamePage
import org.keizar.android.ui.game.rememberGameBoardViewModel
import org.keizar.client.GameRoom
import org.keizar.client.RemoteGameSession
import org.keizar.game.BoardProperties

@Composable
fun MultiplayerGamePage(
    roomId: UInt,
    onClickHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundScope = remember { CoroutineScopeOwner() }
    val sessionFlow = remember {
        MutableStateFlow<RemoteGameSession?>(null)
    }
    val session by sessionFlow.collectAsStateWithLifecycle()

    if (session == null) {
        ConnectingRoomDialog()
    }

    LaunchedEffect(roomId) {
        sessionFlow.emit(
            RemoteGameSession.createAndConnect(
                // TODO: pass compilation
                GameRoom(
                    roomId,
                    BoardProperties.getStandardProperties(0)
                ), backgroundScope.scope.coroutineContext
            )
        )
    }

    session?.let {
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

class CoroutineScopeOwner : RememberObserver {
    val scope = CoroutineScope(SupervisorJob())
    override fun onAbandoned() {
        scope.cancel()
    }

    override fun onForgotten() {
        scope.cancel()
    }

    override fun onRemembered() {
    }
}