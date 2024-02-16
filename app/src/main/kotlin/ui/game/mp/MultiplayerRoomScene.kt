package org.keizar.android.ui.game.mp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import org.keizar.android.ui.foundation.AbstractViewModel
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.client.KeizarClientFacade
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.seconds


class MultiplayerRoomViewModel(
    private val roomId: UInt,
) : AbstractViewModel(), KoinComponent {
    private val facade: KeizarClientFacade by inject()
    val playersReady = flow {
        while (currentCoroutineContext().isActive) {
            emit(facade.getRoom(roomId).playersReady)
            delay(2.seconds)
        }
    }.flowOn(Dispatchers.IO)
        .distinctUntilChanged()
        .shareInBackground()
}

@Composable
fun MultiplayerRoomScene(
    roomId: UInt,
    onClickHome: () -> Unit,
    onPlayersReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = remember(roomId) {
        MultiplayerRoomViewModel(roomId)
    }

    val playersReady by vm.playersReady.collectAsStateWithLifecycle(false)
    SideEffect {
        if (playersReady) {
            onPlayersReady()
        }
    }

    val clipboardManager = LocalClipboardManager.current
    LaunchedEffect(roomId) {
        clipboardManager.setText(AnnotatedString("P-$roomId"))
    }

    MultiplayerRoomPage(roomId, onClickHome, modifier)
}

@Composable
private fun MultiplayerRoomPage(
    roomId: UInt,
    onClickHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(),
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.padding(bottom = 32.dp)) {
                Text(text = "Waiting for other players", style = MaterialTheme.typography.titleMedium)
            }
            Row {
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
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .weight(1f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewMultiplayerRoomPage() {
    ProvideCompositionalLocalsForPreview {
        MultiplayerRoomPage(roomId = 12345u, onClickHome = { })
    }
}