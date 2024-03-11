package org.keizar.android.ui.game

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.keizar.android.data.GameStartConfigurationEncoder
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.game.actions.GameBoardTopBar
import org.keizar.android.ui.game.actions.TurnStatusIndicator
import org.keizar.android.ui.game.actions.UndoButton
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.configuration.createBoard
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.utils.communication.game.Difficulty
import org.keizar.utils.communication.game.Player

@Composable
fun BaseGamePage(
    vm: GameBoardViewModel,
    onClickHome: () -> Unit,
    onClickNewGame: () -> Unit,
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
            val savedSeed by remember {
                mutableStateOf(GameStartConfigurationEncoder.encode(vm.startConfiguration))
            }
            TopAppBar(
                title = {
                    if (isSystemInLandscape()) {
                        Box(Modifier.fillMaxWidth()) {
                            Text(text = "Game")
                            TurnStatusIndicator(vm, Modifier.align(Alignment.Center))
                        }
                    } else {
                        Text(text = "Game")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClickHome) {
                        Icon(Icons.Rounded.Home, contentDescription = "Back")
                    }
                },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {

                        DropdownMenuItem(onClick = {
                            showMenu = false
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip =
                                ClipData.newPlainText("Copied seed", savedSeed)
                            clipboard.setPrimaryClip(clip)
                        }) {
                            Text("Copy seed")
                        }
                        DropdownMenuItem(onClick = {
                            showMenu = false
                            vm.launchInBackground {
                                try {
                                    vm.addSeed(savedSeed)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Game board saved", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            "Failed to save, please check your network connection",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    throw e
                                }
                            }
                        }) {
                            Text("Save Board")
                        }
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
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BoxWithConstraints(Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                        val size = min(maxWidth, maxHeight) - 240.dp
                        GameBoardScaffold(
                            vm,
                            topBar = { GameBoardTopBar(vm, turnStatusIndicator = null) },
                            board = { board(size) },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(IntrinsicSize.Min),
                            bottomBar = { DialogsAndBottomBar(vm, onClickHome, onClickNewGame, onClickLogin) },
                            actions = {},
                        )
                    }
                }
                Row(Modifier.align(Alignment.BottomEnd)) {
                    actions()
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
                                    bottomBar = {
                                        DialogsAndBottomBar(
                                            vm,
                                            onClickHome,
                                            onClickNewGame,
                                            onClickLogin
                                        )
                                    },
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
    ProvideCompositionalLocalsForPreview {
        val vm = rememberSinglePlayerGameBoardForPreview()
        BaseGamePage(
            vm = vm,
            onClickHome = {},
            onClickNewGame = {},
            onClickLogin = {},
            actions = {
                UndoButton(vm = vm)
            }
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