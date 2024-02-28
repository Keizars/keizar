package org.keizar.android.ui.home

import android.app.Activity
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.get
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import org.keizar.android.R
import org.keizar.android.client.SessionManager
import org.keizar.android.data.SavedState
import org.keizar.android.data.SavedStateRepository
import org.keizar.android.tutorial.Tutorials
import org.keizar.android.ui.about.AboutScene
import org.keizar.android.ui.game.configuration.GameConfigurationScene
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.configuration.createBoard
import org.keizar.android.ui.game.mp.MatchViewModel
import org.keizar.android.ui.game.mp.MultiplayerGamePage
import org.keizar.android.ui.game.mp.MultiplayerLobbyScene
import org.keizar.android.ui.game.mp.room.MultiplayerRoomScene
import org.keizar.android.ui.game.sp.SinglePlayerGameScene
import org.keizar.android.ui.profile.AuthScene
import org.keizar.android.ui.profile.ProfileScene
import org.keizar.android.ui.profile.ProfileViewModel
import org.keizar.android.ui.rules.RuleBookPage
import org.keizar.android.ui.rules.RuleReferencesScene
import org.keizar.android.ui.tutorial.TutorialScene
import org.keizar.android.ui.tutorial.TutorialSelectionDialog
import org.keizar.android.ui.tutorial.TutorialSelectionPage
import org.keizar.android.ui.tutorial.TutorialSelectionScene
import org.keizar.game.GameSession
import org.keizar.game.snapshot.GameSnapshot
import org.koin.core.context.GlobalContext

private val json = Json { ignoreUnknownKeys = true }

@Composable
@Preview(showBackground = true)
@Preview(showBackground = true, device = Devices.TABLET)
fun MainScreen() {
    val navController = rememberNavController()


    LaunchedEffect(true) {
        val repository = GlobalContext.get().get<SavedStateRepository>()
        when (val savedState = repository.savedState.first()) {
            SavedState.Empty -> {}
            is SavedState.SinglePlayerGame -> {
                val configuration = savedState.configuration
                navController.navigate(navController.graph["game/single-player"].id, Bundle().apply {
                    putString(
                        "configuration",
                        ProtoBuf.encodeToHexString(GameStartConfiguration.serializer(), configuration)
                    )
                    putString("savedSnapshot", json.encodeToString(GameSnapshot.serializer(), savedState.snapshot))
                })
            }
        }
    }

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
            listOf(
                navArgument("configuration") {
                    nullable = false
                    type = NavType.StringType
                },
                navArgument("savedSnapshot") {
                    nullable = true
                    type = NavType.StringType
                },
            ),
        ) { entry ->
            entry.arguments?.getString("configuration")?.let {
                val configuration = remember {
                    ProtoBuf.decodeFromHexString(GameStartConfiguration.serializer(), it)
                }

                val session = remember(entry.arguments?.getString("savedSnapshot")) {
                    val snapshot = entry.arguments?.getString("savedSnapshot")?.let {
                        kotlin.runCatching { json.decodeFromString(GameSnapshot.serializer(), it) }
                            .onFailure { it.printStackTrace() }
                            .getOrNull()
                    }

                    if (snapshot == null) {
                        GameSession.create(configuration.createBoard())
                    } else {
                        GameSession.restore(snapshot)
                    }
                }

                SinglePlayerGameScene(
                    startConfiguration = configuration,
                    navController = navController,
                    session = session,
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
                onRoomCreated = { roomId ->
                    navController.navigate(navController.graph["game/room"].id, Bundle().apply {
                        putString("roomId", roomId)
                    })
                },
                Modifier.fillMaxSize(),
                vm = matchViewModel,
            )
        }
        composable("game/room") { backStackEntry ->
            val roomId = backStackEntry.arguments!!.getString("roomId")!!.toUInt()
            MultiplayerRoomScene(
                roomId = roomId,
                onClickHome = { navController.navigate("home") },
                onPlayersReady = {
                    navController.navigate(navController.graph["game/multiplayer"].id, Bundle().apply {
                        putString("roomId", roomId.toString())
                    })
                },
                Modifier.fillMaxSize(),
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
        composable(
            "rules/{page}",
            arguments = listOf(navArgument("page") { type = NavType.StringType })
        ) { entry ->
            val page = entry.arguments?.getString("page").let {
                when (it?.lowercase()) {
                    "rules" -> RuleBookPage.RULES
                    "symbols" -> RuleBookPage.SYMBOLS
                    else -> RuleBookPage.RULES
                }
            }
            RuleReferencesScene(
                onClickBack = { navController.popBackStack() },
                page = page,
            )
        }
        composable(
            "tutorial/{tutorialId}",
            arguments = listOf(navArgument("tutorialId") { type = NavType.StringType })
        ) { entry ->
            val tutorialId = entry.arguments?.getString("tutorialId")!!
            val tutorial = Tutorials.getById(tutorialId)
            TutorialScene(
                tutorial,
                navController = navController,
            )
        }
        composable("tutorials") {
            TutorialSelectionScene(
                onClickBack = {
                    navController.popBackStack()
                },
                onClickRuleBook = {
                    when (it) {
                        RuleBookPage.RULES -> navController.navigate("rules/rules")
                        RuleBookPage.SYMBOLS -> navController.navigate("rules/symbols")
                    }
                },
                onClickTutorial = {
                    navController.navigate("tutorial/$it")
                }
            )
        }
        composable(
            "about",
        ) {
            AboutScene(
                onClickBack = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            "auth/{mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) {
            AuthScene(
                initialIsRegister = it.arguments?.getString("mode") == "register",
                onClickBack = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            "profile",
        ) {
            SideEffect {
                if (GlobalContext.get().get<SessionManager>().token.value == null) {
                    navController.navigate("auth/login")
                }
            }
            ProfileScene(
                remember {
                    ProfileViewModel()
                },
                onClickBack = {
                    navController.popBackStack()
                },
                onClickEdit = {
                    navController.navigate("profile/edit")
                }
            )
        }
    }
}

@Composable
fun HomePage(navController: NavController) {
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Title or logo can be added here
        Box(modifier = Modifier
            .weight(1f)
            .widthIn(max = 256.dp), contentAlignment = Alignment.Center) {
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
            Button(onClick = { navController.navigate("profile") }, modifier = Modifier.width(170.dp)) {
                Text("Account", textAlign = TextAlign.Center)
            }

            // Tutorial Button
            var showTutorialSheet by remember { mutableStateOf(false) }

            if (showTutorialSheet) {
                TutorialSelectionDialog(onDismissRequest = {
                    showTutorialSheet = false
                }) {
                    TutorialSelectionPage(
                        onClickTutorial = {
                            showTutorialSheet = false
                            navController.navigate("tutorial/$it")
                        },
                        onClickRuleBook = {
                            showTutorialSheet = false
                            when (it) {
                                RuleBookPage.RULES -> navController.navigate("rules/rules")
                                RuleBookPage.SYMBOLS -> navController.navigate("rules/symbols")
                            }
                        },
                        modifier = Modifier.padding(all = 16.dp),
                    )
                }
            }

            Button(onClick = {
                showTutorialSheet = true
            }, modifier = Modifier.width(170.dp)) {
                Text("Demo/Rules", textAlign = TextAlign.Center)
            }

            Button(onClick = { navController.navigate("about") }, modifier = Modifier.width(170.dp)) {
                Text("About", textAlign = TextAlign.Center)
            }

            val context = LocalContext.current
            Button(onClick = { if (context is Activity) context.finish() }, modifier = Modifier.width(170.dp)) {
                Text("Exit", textAlign = TextAlign.Center)
            }
        }

        Column {
            CopyrightText()
        }
    }
}

@Composable
fun CopyrightText() {
    Text(
        "© Zuboard Games 2024",
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium
    )
}

