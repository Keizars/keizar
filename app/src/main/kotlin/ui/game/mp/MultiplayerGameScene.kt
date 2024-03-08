package org.keizar.android.ui.game.mp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import org.keizar.android.BuildConfig
import org.keizar.android.ui.external.placeholder.placeholder
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.ErrorDialogHost
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.game.BaseGamePage
import org.keizar.android.ui.game.GameBoard
import org.keizar.android.ui.game.MultiplayerGameBoardViewModel
import org.keizar.android.ui.game.mp.room.ConnectingRoomDialog
import org.keizar.android.ui.game.transition.CapturedPiecesHost
import org.keizar.android.ui.profile.component.AvatarImage
import org.keizar.client.ClientPlayer
import org.keizar.game.snapshot.buildGameSession
import org.keizar.utils.communication.game.Player


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

    val error by connector.connectionError.collectAsStateWithLifecycle()
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

    ErrorDialogHost(errorFlow = connector.error, onClickCancel = {
        onClickHome()
    })

    val client by connector.client.collectAsStateWithLifecycle(null)
    val session by connector.session.collectAsStateWithLifecycle(null)

    if (client == null) {
        ConnectingRoomDialog(extra = {
            if (BuildConfig.DEBUG) {
                Text(text = "Debug info: client is still null")
            }
        })
    }

    client?.let { c ->
        session?.let { s ->
            val vm = remember(s, c) {
                MultiplayerGameBoardViewModel(s, s.player, c.selfPlayer, c.opponentPlayer)
            }

            MultiplayerGamePage(vm, onClickHome, onClickGameConfig, modifier)
        }
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
                    val opponentName by vm.opponentUser.collectAsState(null)
                    Box(modifier = Modifier.clip(CircleShape)) {
                        AvatarImage(
                            url = opponentName?.avatarUrlOrDefault(),
                            modifier = Modifier.size(tileSize),
                        )
                    }

                    Text(
                        text = opponentName?.nickname ?: "placeholder",
                        Modifier
                            .padding(horizontal = 8.dp)
                            .placeholder(opponentName == null),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    CapturedPiecesHost(
                        capturedPieceHostState = vm.theirCapturedPieceHostState,
                        slotSize = tileSize,
                        sourceCoordinates = sourceCoordinates,
                    )
                },
                myCapturedPieces = { tileSize, sourceCoordinates ->
                    val selfName by vm.myUser.collectAsState(null)
                    CapturedPiecesHost(
                        capturedPieceHostState = vm.myCapturedPieceHostState,
                        slotSize = tileSize,
                        sourceCoordinates = sourceCoordinates,
                    )
                    Text(
                        text = selfName?.nickname ?: "placeholder",
                        Modifier
                            .padding(horizontal = 8.dp)
                            .placeholder(selfName == null),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Box(modifier = Modifier.clip(CircleShape)) {
                        AvatarImage(
                            url = selfName?.avatarUrlOrDefault(),
                            modifier = Modifier.size(tileSize),
                        )
                    }
                },
            )
        }
    )
}

class CoroutineScopeOwner : RememberObserver, AbstractViewModel() {
    val scope get() = backgroundScope
}

@Preview
@Preview(device = Devices.TABLET)
@Composable
private fun PreviewMultiplayerGame() {
    val vm = remember {
        MultiplayerGameBoardViewModel(
            buildGameSession {},
            Player.FirstWhitePlayer,
            selfClientPlayer = ClientPlayer("me", isHost = true, initialIsReady = true),
            opponentClientPlayer = MutableStateFlow(ClientPlayer("other", isHost = false, initialIsReady = true)),
        )
    }
    ProvideCompositionalLocalsForPreview {
        MultiplayerGamePage(
            vm,
            onClickHome = {},
            onClickGameConfig = {},
        )
    }
}