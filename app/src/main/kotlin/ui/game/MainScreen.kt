package org.keizar.android.ui.game

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.keizar.game.BoardProperties

@Composable
@Preview(showBackground = true)
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title or logo can be added here
        Box(modifier = Modifier) {
            Icon(Icons.Default.Chair, "KEIZÁR")
        }

        // Single Player Button
        Button(onClick = { /* TODO: Handle Single Player click */ }) {
            Text("Single Player")
        }

        Spacer(modifier = Modifier.height(10.dp)) // Adds space between buttons

        // Multiplayer Button
        Button(onClick = { /* TODO: Handle Multiplayer click */ }) {
            Text("Multiplayer")
        }

    }
}