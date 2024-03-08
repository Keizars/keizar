package org.keizar.android.ui.game

import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.keizar.android.data.encode
import org.keizar.android.ui.KeizarApp
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.configuration.createBoard
import org.keizar.game.Difficulty
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.utils.communication.game.Player

@Composable
fun BaseGamePage(
    vm: GameBoardViewModel,
    onClickHome: () -> Unit,
    onClickGameConfig: () -> Unit,
    onClickLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
    board: @Composable (Dp) -> Unit = @Composable { size ->
        GameBoard(
            vm = vm,
            Modifier
                .padding(vertical = 16.dp)
                .size(size),
        )
    },
    actions: @Composable RowScope.() -> Unit = {},
) {
    var showGameConfigurationDialog by remember { mutableStateOf(false) }
    if (showGameConfigurationDialog) {
        AlertDialog(
            onDismissRequest = { showGameConfigurationDialog = false },
            confirmButton = {
                TextButton(onClick = { showGameConfigurationDialog = false }) {
                    Text(text = "OK")
                }
            },
            dismissButton = null,
            title = { Text(text = "Copied seed to clipboard") },
            text = { Text(text = "Share it with your friends and they can start with the same layout.") },
        )
    }

    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            val clipboard = LocalClipboardManager.current
            TopAppBar(
                title = { Text(text = "Game") },
                navigationIcon = {
                    IconButton(onClick = onClickHome) {
//                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        Icon(Icons.Rounded.Home, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showGameConfigurationDialog = true
                        clipboard.setText(AnnotatedString(vm.startConfiguration.encode()))
                    }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                }
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isSystemInLandscape()) {
                BoxWithConstraints(Modifier.padding(horizontal = 16.dp)) {
                    val size = maxHeight - 240.dp
                    GameBoardScaffold(
                        vm,
                        board = { board(size) },
                        modifier = Modifier.width(IntrinsicSize.Min),
                        bottomBar = { DialogsAndBottomBar(vm, onClickHome, onClickGameConfig, onClickLogin) },
                        actions = actions,
                    )
                }
            } else {
                BoxWithConstraints {
                    val size = min(maxWidth, maxHeight)
                    CompositionLocalProvider(
                        LocalOverscrollConfiguration provides null
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                GameBoardScaffold(
                                    vm,
                                    board = { board(size) },
                                    modifier = Modifier.fillMaxSize(),
                                    bottomBar = { DialogsAndBottomBar(vm, onClickHome, onClickGameConfig, onClickLogin) },
                                    actions = actions,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun PreviewGamePage() {
    KeizarApp {
        BaseGamePage(
            vm = rememberSinglePlayerGameBoardForPreview(),
            onClickHome = {},
            onClickGameConfig = {},
            onClickLogin = {},
        )
    }
}

@Composable
fun rememberSinglePlayerGameBoardForPreview() = rememberSinglePlayerGameBoardViewModel(
    session = remember {
        GameSession.create(
            GameStartConfiguration(
                difficulty = Difficulty.EASY,
                layoutSeed = 0,
                playAs = Role.WHITE,
            ).createBoard()
        )
    },
    selfPlayer = Player.FirstWhitePlayer,
)