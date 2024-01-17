package org.keizar.android.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.keizar.android.R
import org.keizar.android.ui.game.GamePage

@Composable
@Preview(showBackground = true)
fun MainScreen() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home") {
        composable("home") { HomePage(navController) }
        composable("single player game") { GamePage() }
    }
}

@Composable
fun HomePage(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title or logo can be added here
        Box(modifier = Modifier) {
            Icon(
                painter = painterResource(id = R.drawable.keizar_icon),
                contentDescription = "Keizar Icon",
                tint = Color.Unspecified
            )
        }

        Spacer(modifier = Modifier.size(80.dp))

        // Single Player Button
        Button(onClick = { navController.navigate("single player game") }, modifier = Modifier.width(170.dp)) {
            Text("Play vs Computer")
        }


        Spacer(modifier = Modifier.size(50.dp))

        // Multiplayer Button
        Button(onClick = { /* TODO: Handle Multiplayer click */ }, modifier = Modifier.width(170.dp)) {
            Text("2 players")
        }

        Spacer(modifier = Modifier.size(50.dp))

        // Saved game Button
        Button(onClick = { /* TODO: Handle Multiplayer click */ }, modifier = Modifier.width(170.dp)) {
            Text("Saved games")
        }

        Spacer(modifier = Modifier.size(50.dp))

        // Tutorial Button
        Button(onClick = { /* TODO: Handle Multiplayer click */ }, modifier = Modifier.width(170.dp)) {
            Text("Tutorial")
        }
    }
}