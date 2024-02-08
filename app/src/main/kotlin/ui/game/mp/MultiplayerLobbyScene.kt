package org.keizar.android.ui.game.mp

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import org.keizar.android.ui.foundation.launchInBackground

@Composable
fun MultiplayerLobbyScene(
    onClickHome: () -> Unit,
    onJoinGame: (roomId: String) -> Unit,
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

            Column(Modifier.padding(16.dp)) {
                OnlineMatchingSection(Modifier.weight(1f))

                PlayWithFriendsSection(vm, onJoinGame)
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
            Text(text = "Not yet supported", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun PlayWithFriendsSection(
    vm: MatchViewModel,
    onJoinRoom: (roomId: String) -> Unit,
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

        Row(
            Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val roomId by vm.joinRoomIdEditing.collectAsState()
            OutlinedTextField(
                value = roomId,
                onValueChange = { vm.setJoinRoomId(it) },
                label = { Text("Join Their Room") },
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

            Button(
                onClick = {
                    onJoinRoom(roomId)
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
                .height(92.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val selfRoomId by vm.selfRoomId.collectAsState()
            androidx.compose.animation.AnimatedVisibility(visible = selfRoomId == null, exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "You can also", style = MaterialTheme.typography.bodyMedium)

                    OutlinedButton(
                        onClick = { vm.launchInBackground { createSelfRoom() } },
                        Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(text = "Create a Room")
                    }

                    Text(text = "and invite others.", style = MaterialTheme.typography.bodyMedium)
                }
            }
            androidx.compose.animation.AnimatedVisibility(visible = selfRoomId != null, enter = fadeIn()) {
                Row {
                    OutlinedTextField(
                        value = selfRoomId ?: "",
                        onValueChange = { },
                        label = { Text("Your Room ID") },
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true,
                        trailingIcon = {
                            val clipboard = LocalClipboardManager.current
                            IconButton(onClick = {
                                clipboard.setText(AnnotatedString("P-$selfRoomId"))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Paste")
                            }
                        },
                        prefix = { Text(text = "P-") },
                        supportingText = { Text("Share this ID with your friends to play in the same game.") },
                        singleLine = true,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .weight(1f),
                    )

//                    Button(onClick = { }, Modifier.padding(start = 8.dp, top = 16.dp)) {
//                        Icon(
//                            Icons.Outlined.Share,
//                            contentDescription = "Share",
//                            Modifier
//                                .padding(end = 6.dp)
//                                .size(16.dp)
//                        )
//                        Text(text = "Share")
//                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewMatchPage(
    @PreviewParameter(BooleanProvider::class) roomCreated: Boolean
) {
    MultiplayerLobbyScene(
        {},
        {},
        vm = remember {
            MatchViewModel().apply {
                if (roomCreated) {
                    launchInBackground { createSelfRoom() }
                }
                setJoinRoomId("123456")
            }
        }
    )
}

private open class BooleanProvider : CollectionPreviewParameterProvider<Boolean>(listOf(false, true))

@Preview(showBackground = true)
@Composable
private fun PreviewRoomsForJoin(
    @PreviewParameter(BooleanProvider::class) hasJoin: Boolean
) {
    PlayWithFriendsSection(
        vm = remember {
            MatchViewModel().apply {
                if (hasJoin) {
                    setJoinRoomId("123456")
                }
            }
        },
        {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewRoomsForHostingRoom(
    @PreviewParameter(BooleanProvider::class) roomCreated: Boolean
) {
    PlayWithFriendsSection(
        vm = remember {
            MatchViewModel().apply {
                if (roomCreated) {
                    launchInBackground { createSelfRoom() }
                }
            }
        },
        {}
    )
}
