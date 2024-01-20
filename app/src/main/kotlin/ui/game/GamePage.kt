package org.keizar.android.ui.game

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.keizar.game.BoardProperties
import kotlin.random.Random

@Composable
fun GamePage(
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Game") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
//                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            Icon(Icons.Rounded.Home, contentDescription = "Back")
                        }
                    },
                )
            }
        ) { contentPadding ->
            Column(modifier = Modifier.padding(contentPadding)) {
                BoxWithConstraints {
                    GameBoard(
                        properties = BoardProperties.getStandardProperties(Random(0)),
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .size(min(maxWidth, maxHeight)),
                    )
                }
            }
        }
    }
}