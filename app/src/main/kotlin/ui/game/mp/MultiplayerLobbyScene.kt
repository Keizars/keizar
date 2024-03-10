package org.keizar.android.ui.game.mp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.keizar.android.ui.foundation.ErrorDialogHost
import org.keizar.android.ui.foundation.ErrorMessage
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.game.mp.room.ConnectingRoomDialog
import retrofit2.HttpException

/**
 * 2-player lobby scene, where players can join a room or create a room.
 */
@Composable
fun MultiplayerLobbyScene(
    onClickHome: () -> Unit,
    onJoinGame: (roomId: String) -> Unit,
    onRoomCreated: (roomId: String) -> Unit,
    modifier: Modifier = Modifier,
    vm: MatchViewModel = remember {
        MatchViewModel()
    }
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Lobby") },
                navigationIcon = {
                    IconButton(onClick = onClickHome) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(contentPadding)
        ) {
            ErrorDialogHost(errorFlow = vm.error, clearError = true)
//            val pagerState = rememberPagerState { 2 }
//            val uiScope = rememberCoroutineScope()
//            PrimaryTabRow(selectedTabIndex = pagerState.currentPage, Modifier.fillMaxWidth()) {
//                Tab(
//                    text = { Text("Private") },
//                    selected = pagerState.currentPage == 0,
//                    onClick = {
//                        uiScope.launch { pagerState.animateScrollToPage(0) }
//                    }
//                )
//                Tab(
//                    text = { Text("Match") },
//                    selected = pagerState.currentPage == 1,
//                    onClick = { uiScope.launch { pagerState.animateScrollToPage(1) } }
//                )
//            }
//            HorizontalPager(state = pagerState, Modifier.weight(1f)) { page ->
//                Box(modifier = Modifier.fillMaxSize()) {
//                    Text(text = page.toString())
//                }
//            }

            if (isSystemInLandscape()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .widthIn(max = 400.dp)
                    ) {
//                        OnlineMatchingSection(Modifier.weight(1f))

                        Box(
                            Modifier
                                .fillMaxHeight()
                                .weight(1f), contentAlignment = Alignment.Center
                        ) {
                            PlayWithFriendsSection(vm, onJoinGame, onRoomCreated, Modifier)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(Modifier.padding(16.dp)) {
//                    OnlineMatchingSection(Modifier.weight(1f))

                        PlayWithFriendsSection(vm, onJoinGame, onRoomCreated)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnlineMatchingSection(modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = "Online Matching",
            Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.titleLarge
        )

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Coming soon", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun PlayWithFriendsSection(
    vm: MatchViewModel,
    onJoinRoom: (roomId: String) -> Unit,
    onRoomCreated: (roomId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = "Private Rooms",
            Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Play with friends in a private room. ",
            style = MaterialTheme.typography.labelMedium
        )

        var joiningRoom by remember {
            mutableStateOf(false)
        }
        if (joiningRoom) {
            ConnectingRoomDialog()
        }

        Row(
            Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val roomId by vm.joinRoomIdEditing.collectAsState()
            OutlinedTextField(
                value = roomId,
                onValueChange = { vm.setJoinRoomId(it) },
                label = { Text("Join Room") },
                shape = RoundedCornerShape(12.dp),
                keyboardActions = KeyboardActions {
                    onJoinRoom(roomId)
                },
                trailingIcon = {
                    val clipboard = LocalClipboardManager.current
                    IconButton(onClick = {
                        clipboard.getText()?.text?.let { vm.setJoinRoomId(it) }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste")
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Go),
                prefix = { Text(text = "P-") },
                singleLine = true,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .weight(1f),
            )

            var showRoomFullDialog by remember { mutableStateOf(false) }
            if (showRoomFullDialog) {
                AlertDialog(
                    onDismissRequest = { showRoomFullDialog = false },
                    confirmButton = {
                        Button(onClick = { showRoomFullDialog = false }) { Text(text = "OK") }
                    },
                    text = { Text(text = "Room is full or does not exist") }
                )
            }

            Button(
                onClick = {
                    vm.launchInBackground {
                        joiningRoom = true
                        try {
                            vm.joinRoom()
                            withContext(Dispatchers.Main) {
                                onJoinRoom(roomId)
                            }
                        } catch (e: HttpException) {
                            Log.e(null, "Error in joinRoom", e)
                            if (e.code() == 500) {
                                // room full
                                showRoomFullDialog = true
                            }
                        } catch (e: Exception) {
                            error.value = ErrorMessage.networkError()
                            throw e
                        } finally {
                            joiningRoom = false
                        }
                    }
                },
                Modifier.padding(start = 8.dp, top = 8.dp),
                enabled = roomId.isNotBlank(),
            ) {
                Text(text = "Join")
            }
        }

        HorizontalDivider(Modifier.padding(horizontal = 2.dp, vertical = 8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .heightIn(min = 92.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "You can also", style = MaterialTheme.typography.bodyMedium)

                OutlinedButton(
                    onClick = { vm.launchInBackground { createSelfRoom() } },
                    Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(text = "Create a Room")
                }


                vm.selfRoomId.collectAsStateWithLifecycle().value?.let { roomId ->
                    SideEffect {
                        onRoomCreated(roomId)
                    }
                }

                Text(text = "and invite others.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview
@Preview(device = Devices.TABLET)
@Composable
private fun PreviewMatchPage(
    @PreviewParameter(BooleanProvider::class) roomCreated: Boolean
) {
    ProvideCompositionalLocalsForPreview {
        MultiplayerLobbyScene(
            {},
            {},
            {},
            vm = remember {
                MatchViewModel().apply {
                    if (roomCreated) {
                        launchInBackground { createSelfRoom() }
                    }
                    setJoinRoomId("123456")
                }
            })
    }
}

private open class BooleanProvider : CollectionPreviewParameterProvider<Boolean>(listOf(false, true))

@Preview(showBackground = true, heightDp = 250)
@Composable
private fun PreviewRoomsForJoin(
    @PreviewParameter(BooleanProvider::class) hasJoin: Boolean
) {
    ProvideCompositionalLocalsForPreview {
        PlayWithFriendsSection(
            vm = remember {
                MatchViewModel().apply {
                    if (hasJoin) {
                        setJoinRoomId("123456")
                    }
                }
            },
            onJoinRoom = {},
            onRoomCreated = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 250)
@Composable
private fun PreviewRoomsForHostingRoom(
    @PreviewParameter(BooleanProvider::class) roomCreated: Boolean
) {
    ProvideCompositionalLocalsForPreview {
        PlayWithFriendsSection(
            vm = remember {
                MatchViewModel().apply {
                    if (roomCreated) {
                        launchInBackground { createSelfRoom() }
                    }
                }
            },
            onJoinRoom = {},
            onRoomCreated = {},
        )
    }
}
