package org.keizar.android.ui.game.mp.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.keizar.android.ui.foundation.ErrorDialogHost
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.game.configuration.BoardLayoutPreview
import org.keizar.android.ui.game.configuration.BoardSeedTextField
import org.keizar.android.ui.profile.component.AvatarImage
import org.keizar.game.Role


/**
 * Private room where two players can see each other's name and change seeds.
 */
@Composable
fun PrivateRoomScene(
    roomId: UInt,
    onClickHome: () -> Unit,
    onPlayersReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(roomId) {
        PrivateRoomViewModelImpl(roomId)
    }

    val playersReady by vm.playersReady.collectAsStateWithLifecycle(false)
    if (playersReady) {
        SideEffect {
            onPlayersReady()
        }
    }

    val clipboardManager = LocalClipboardManager.current
    LaunchedEffect(roomId) {
        clipboardManager.setText(AnnotatedString("P-$roomId"))
    }

    PrivateRoomPage(vm, onClickHome, modifier)
}

@Composable
private fun AcceptArea(
    vm: PrivateRoomViewModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.wrapContentHeight(),
        horizontalArrangement = Arrangement.End,
    ) {
        val accept by vm.selfReady.collectAsStateWithLifecycle(false)
        val opponentPlayer by vm.opponentPlayer.collectAsStateWithLifecycle(null)
        if (!accept && opponentPlayer != null) {
            Button(
                modifier = if (!isSystemInLandscape()) Modifier.padding(end = 12.dp) else Modifier,
                onClick = { vm.backgroundScope.launch { vm.ready() } }
            ) {
                Text(text = "Ready!")
            }
        }
    }
}

@Composable
private fun PrivateRoomPage(
    vm: PrivateRoomViewModel,
    onClickHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Private Room") },
                navigationIcon = {
                    IconButton(onClick = onClickHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        ErrorDialogHost(errorFlow = vm.errorDialog)

        if (isSystemInLandscape()) {
            Row(
                Modifier
                    .systemBarsPadding()
                    .padding(
                        PaddingValues(
                            start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                            top = contentPadding.calculateTopPadding(),
                            end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                        )
                    )
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    val properties by vm.configuration.boardProperties.collectAsStateWithLifecycle(
                        null
                    )
                    BoardLayoutPreview(
                        boardProperties = properties,
                        playAs = Role.WHITE,
                    )
                }
                Column(
                    Modifier.wrapContentWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(Modifier.widthIn(max = 360.dp)) {
                        Configurations(vm)
                        AcceptArea(vm, Modifier)
                    }
                }
            }
        } else {
            Box(Modifier) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    val properties by vm.configuration.boardProperties.collectAsStateWithLifecycle(
                        null
                    )
                    Spacer(
                        modifier = Modifier
                            .statusBarsPadding()
                    )

                    BoardLayoutPreview(
                        boardProperties = properties,
                        playAs = Role.WHITE,
                    )

                    Configurations(vm, Modifier.padding(top = 8.dp))

                    Spacer(modifier = Modifier.height(72.dp))
                }

                if (!isSystemInLandscape()) {
                    Column(Modifier.align(Alignment.BottomCenter)) {
                        HorizontalDivider()
                        AcceptArea(
                            vm,
                            Modifier
                                .fillMaxWidth()
                                .alpha(0.96f)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 8.dp)
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Configurations(
    vm: PrivateRoomViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier
                .padding(bottom = 16.dp)
                .padding(start = 8.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = "Board Configuration",
                style = MaterialTheme.typography.titleMedium
            )
        }

        val isHost = vm.selfIsHost.collectAsStateWithLifecycle(false).value

        BoardSeedTextField(
            text = vm.configuration.configurationSeedText.collectAsStateWithLifecycle().value,
            onValueChange = { vm.configuration.setConfigurationSeedText(it) },
            onClickRandom = {
                vm.backgroundScope.launch {
                    vm.configuration.updateRandomSeed()
                    vm.boardChangeFromOtherClientUpdate()
                }
            },
            isError = vm.configuration.isConfigurationSeedTextError.collectAsStateWithLifecycle(
                false
            ).value,
            refreshEnable = vm.configuration.freshButtonEnable.collectAsStateWithLifecycle(true).value && isHost,
            readOnly = true,// vm.selfIsHost.collectAsStateWithLifecycle(false).value,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                Text(text = "Explore new board layouts by changing the seed. Other player can also see this board.")
            },
        )

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        val opponentUser by vm.opponentUser.collectAsStateWithLifecycle(null)
        ActionArea(
            roomId = vm.roomId,
            opponentName = opponentUser?.nickname,
            opponentAvatar = opponentUser?.avatarUrlOrDefault(),
            opponentIsReady = vm.opponentReady.collectAsStateWithLifecycle(false).value
        )
    }
}

@Composable
private fun ActionArea(
    roomId: UInt,
    opponentName: String?,
    opponentAvatar: String?,
    opponentIsReady: Boolean
) {
    if (opponentName == null) {
        Row(
            Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Waiting for opponent to join...",
                style = MaterialTheme.typography.titleMedium
            )
        }

        RoomIdTextField(
            roomId, Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
        )
    } else {
        val readyMessage = if (opponentIsReady) " is ready!" else " is not ready yet"
        Row(
            Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Opponent:  ",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
            AvatarImage(
                url = opponentAvatar,
                Modifier
                    .clip(CircleShape)
                    .size(32.dp)
            )
            Text(
                text = "$opponentName",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 8.dp)
            )
            Text(
                text = readyMessage,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
            )
        }

    }
}

@Composable
private fun RoomIdTextField(roomId: UInt, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = remember(roomId) { "$roomId" },
        onValueChange = { },
        label = { Text("Your Room ID") },
        shape = RoundedCornerShape(12.dp),
        readOnly = true,
        trailingIcon = {
            val clipboard = LocalClipboardManager.current
            IconButton(onClick = {
                clipboard.setText(AnnotatedString("P-$roomId"))
            }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Paste")
            }
        },
        prefix = { Text(text = "P-") },
        supportingText = { Text("Share this ID with your friends to play in the same game.") },
        singleLine = true,
        modifier = modifier,
    )
}

@Preview
@Preview(heightDp = 800)
@Preview(device = Devices.TABLET)
@Preview(fontScale = 2f)
@Composable
private fun PreviewMultiplayerRoomPage() {
    ProvideCompositionalLocalsForPreview {
        PrivateRoomPage(vm = remember {
            PrivateRoomViewModelImpl(
                roomId = 123u,
            )
        },
            onClickHome = { })
    }
}

@Preview
@Preview(device = Devices.TABLET)
@Preview(fontScale = 2f)
@Composable
private fun PreviewOpponentSetting() {
    ProvideCompositionalLocalsForPreview {
        ActionArea(
            roomId = 123u,
            opponentName = "Test",
            opponentAvatar = "https://ui-avatars.com/api/?name=123",
            opponentIsReady = true
        )
    }
}