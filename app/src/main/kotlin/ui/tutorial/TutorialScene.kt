package org.keizar.android.ui.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.navigation.NavController
import org.keizar.android.tutorial.Tutorial
import org.keizar.android.ui.game.GameBoard

@Composable
fun TutorialScene(
    tutorial: Tutorial,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val vm = remember(tutorial) {
        TutorialGameBoardViewModel(tutorial)
    }
    TutorialPage(
        vm,
        onClickHome = { navController.popBackStack("home", false) },
        modifier
    )
}

@Composable
private fun TutorialPage(
    vm: TutorialGameBoardViewModel,
    onClickHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Game") },
                navigationIcon = {
                    IconButton(onClick = onClickHome) {
                        Icon(Icons.Rounded.Home, contentDescription = "Back")
                    }
                },
                actions = {
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
                    vm,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .size(min(maxWidth, maxHeight)),
                    onClickHome = onClickHome,
                    onClickGameConfig = {},
                )
            }
        }
    }
}