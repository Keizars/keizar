package org.keizar.android.ui.home

import android.app.Activity
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.get
import androidx.navigation.navArgument
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.keizar.android.R
import org.keizar.android.tutorial.Tutorials
import org.keizar.android.ui.game.configuration.GameConfigurationScene
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.mp.MatchViewModel
import org.keizar.android.ui.game.mp.MultiplayerGamePage
import org.keizar.android.ui.game.mp.MultiplayerLobbyScene
import org.keizar.android.ui.game.sp.SinglePlayerGameScene
import org.keizar.android.ui.tutorial.TutorialScene

@Composable
@Preview(showBackground = true)
fun MainScreen() {
    val navController = rememberNavController()

    val matchViewModel = remember {
        MatchViewModel()
    }
    NavHost(navController, startDestination = "home") {
        composable("home") { HomePage(navController) }
        composable(
            "game/configuration",
        ) {
            GameConfigurationScene(navController)
        }
        composable(
            "game/single-player",
            listOf(navArgument("configuration") {
                nullable = false
                type = NavType.StringType
            })
        ) { entry ->
            entry.arguments?.getString("configuration")?.let {
                val configuration = ProtoBuf.decodeFromHexString(GameStartConfiguration.serializer(), it)
                SinglePlayerGameScene(
                    startConfiguration = configuration,
                    navController = navController,
                )
            }
        }
        composable("game/lobby") {
            MultiplayerLobbyScene(
                onClickHome = { navController.navigate("home") },
                onJoinGame = { roomId ->
                    navController.navigate(navController.graph["game/multiplayer"].id, Bundle().apply {
                        putString("roomId", roomId)
                    })
                },
                Modifier.fillMaxSize(),
                vm = matchViewModel,
            )
        }
        composable("game/multiplayer") { backStackEntry ->
            MultiplayerGamePage(
                roomId = backStackEntry.arguments!!.getString("roomId")!!.toUInt(),
                goBack = {
                    navController.popBackStack()
                },
                onClickHome = {
                    navController.navigate("home") {
                        launchSingleTop = true
                    }
                },
                onClickGameConfig = {
                    navController.navigate("game/lobby")
                    matchViewModel.removeSelfRoom()
                },
                Modifier.fillMaxSize()
            )
        }
        composable("saved games") { /* TODO: saved games page*/ }
        composable("tutorial") { entry ->
            val tutorialId = entry.arguments?.getString("tutorialId")!!
            val tutorial = Tutorials.getById(tutorialId)
            TutorialScene(
                tutorial,
                navController = navController,
            )
        }
    }
}

@Composable
fun HomePage(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Title or logo can be added here
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.keizar_icon),
                contentDescription = "Keizar Icon",
                tint = Color.Unspecified
            )
        }

        Column(
            Modifier
                .padding(bottom = 80.dp)
                .weight(2f),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Single Player Button
            Button(onClick = { navController.navigate("game/configuration") }, modifier = Modifier.width(170.dp)) {
                Text("Play vs Computer", textAlign = TextAlign.Center)
            }

            // Multiplayer Button
            Button(onClick = { navController.navigate("game/lobby") }, modifier = Modifier.width(170.dp)) {
                Text("2 Players", textAlign = TextAlign.Center)
            }

            // Saved game Button
            Button(onClick = { /* TODO: Handle saved game click */ }, modifier = Modifier.width(170.dp)) {
                Text("Saved games", textAlign = TextAlign.Center)
            }

            // Tutorial Button
            Button(onClick = { /* TODO: Handle tutorial click */ }, modifier = Modifier.width(170.dp)) {
                Text("Tutorial", textAlign = TextAlign.Center)
            }

            val context = LocalContext.current
            Button(onClick = { if (context is Activity) context.finish() }, modifier = Modifier.width(170.dp)) {
                Text("Exit", textAlign = TextAlign.Center)
            }
        }
    }
}

