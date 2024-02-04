package org.keizar.android.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.navigation.NavController
import org.keizar.android.encode
import org.keizar.android.ui.KeizarApp
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.game.Difficulty
import org.keizar.game.Role

@Composable
fun GameScene(
    startConfiguration: GameStartConfiguration,
    navController: NavController,
) {
    GamePage(startConfiguration, onClickHome = { navController.popBackStack("home", false) },
        onClickGameConfig = { navController.popBackStack("single player game", false) })
}

@Composable
fun GamePage(
    startConfiguration: GameStartConfiguration,
    onClickHome: () -> Unit,
    onClickGameConfig : () -> Unit,
    modifier: Modifier = Modifier,
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
                        clipboard.setText(AnnotatedString(startConfiguration.encode()))
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
            BoxWithConstraints {
                GameBoard(
                    startConfiguration = startConfiguration,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .size(min(maxWidth, maxHeight)),
                    onClickHome = onClickHome,
                    onClickGameConfig = onClickGameConfig
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewGamePage() {
    KeizarApp {
        GamePage(
            startConfiguration = GameStartConfiguration(
                difficulty = Difficulty.EASY,
                layoutSeed = 0,
                playAs = Role.WHITE,
            ),
            onClickHome = {},
            onClickGameConfig = {}
        )
    }
}