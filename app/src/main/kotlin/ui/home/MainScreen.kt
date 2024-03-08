package org.keizar.android.ui.home

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
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
import org.keizar.android.data.GameStartConfigurationEncoder
import org.keizar.android.data.SavedState
import org.keizar.android.data.SavedStateRepository
import org.keizar.android.data.SessionManager
import org.keizar.android.tutorial.Tutorials
import org.keizar.android.ui.about.AboutScene
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.game.configuration.GameConfigurationScene
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.configuration.createBoard
import org.keizar.android.ui.game.mp.MatchViewModel
import org.keizar.android.ui.game.mp.MultiplayerGameScene
import org.keizar.android.ui.game.mp.MultiplayerLobbyScene
import org.keizar.android.ui.game.mp.room.PrivateRoomScene
import org.keizar.android.ui.game.sp.SinglePlayerGameScene
import org.keizar.android.ui.profile.AuthScene
import org.keizar.android.ui.profile.EditPasswordScene
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

/**
 * The content of the main activity.
 *
 * A Screen is a container for a navigation controller, supporting transitions between multiple scenes.
 * The content of a Scene is a Page. The Scene connects Pages, which are unrelated to navigation, to
 * the navigation system.
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current


    LaunchedEffect(true) {
        val repository = GlobalContext.get().get<SavedStateRepository>()
        when (val savedState = repository.savedState.first()) {
            SavedState.Empty -> {}
            is SavedState.SinglePlayerGame -> {
                val configuration = savedState.configuration
                navController.navigate(
                    navController.graph["game/single-player"].id,
                    Bundle().apply {
                        putString(
                            "configuration",
                            ProtoBuf.encodeToHexString(
                                GameStartConfiguration.serializer(),
                                configuration
                            )
                        )
                        putString(
                            "savedSnapshot",
                            json.encodeToString(GameSnapshot.serializer(), savedState.snapshot)
                        )
                    })
            }
        }
    }

    val lobbyViewModel = remember {
        MatchViewModel()
    }
    NavHost(navController, startDestination = "home") {
        composable("home") { HomePage(navController) }
        composable(
            "game/configuration",
            listOf(
                navArgument("configuration") {
                    nullable = true
                    type = NavType.StringType
                },
            )
        ) { entry ->
            val onClickGoBack: () -> Unit = { navController.popBackStack(entry.destination.id, true) }
            BackHandler {
                onClickGoBack()
            }
            GameConfigurationScene(
                initialConfiguration = entry.arguments?.getString("configuration")?.let {
                    GameStartConfigurationEncoder.decode(it)
                } ?: remember {
                    GameStartConfiguration.random()
                },
                onClickGoBack = onClickGoBack,
                navController = navController
            )
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
        composable("game/lobby",
            enterTransition = {
                fadeIn()
            },
            exitTransition = { fadeOut() }
        ) {
            val isLoggedIn by remember {
                GlobalContext.get().get<SessionManager>().isLoggedIn
            }.collectAsStateWithLifecycle(null)
            when (isLoggedIn) {
                null -> {}
                false -> {
                    SideEffect {
                        navController.navigate("auth/login") {
                            launchSingleTop = true
                        }
                    }
                }

                true -> {
                    val onClickHome = {
                        if (lobbyViewModel.selfRoomId.value != null) {
                            lobbyViewModel.removeSelfRoom()
                        }
                        navController.navigate("home")
                    }
                    BackHandler {
                        onClickHome()
                    }
                    MultiplayerLobbyScene(
                        onClickHome = onClickHome,
                        onJoinGame = { roomId ->
                            navController.navigate(
                                navController.graph["game/room"].id,
                                Bundle().apply {
                                    putString("roomId", roomId)
                                })
                        },
                        onRoomCreated = { roomId ->
                            navController.navigate(
                                navController.graph["game/room"].id,
                                Bundle().apply {
                                    putString("roomId", roomId)
                                })
                        },
                        Modifier.fillMaxSize(),
                        vm = lobbyViewModel,
                    )
                }
            }
        }
        composable("game/room") { backStackEntry ->
            LoginChecker(navController)
            val roomId = backStackEntry.arguments!!.getString("roomId")!!.toUInt()

            val onClickHome = {
                if (navController.currentBackStackEntry?.destination?.route == "game/room") {
                    navController.popBackStack("home", false)
                }
                lobbyViewModel.removeSelfRoom()
            }
            BackHandler {
                onClickHome()
            }
            PrivateRoomScene(
                roomId = roomId,
                onClickHome = onClickHome,
                onPlayersReady = {
                    navController.navigate(
                        navController.graph["game/multiplayer"].id,
                        Bundle().apply {
                            putString("roomId", roomId.toString())
                        })
                },
                Modifier.fillMaxSize(),
            )
        }
        composable("game/multiplayer") { backStackEntry ->
            LoginChecker(navController)

            var showConfirmExitDialog by remember { mutableStateOf(false) }
            if (showConfirmExitDialog) {
                AlertDialog(onDismissRequest = { showConfirmExitDialog = false },
                    confirmButton = {
                        Button(onClick = {
                            showConfirmExitDialog = false

                            navController.popBackStack("home", false)
                            lobbyViewModel.removeSelfRoom()
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmExitDialog = false }) {
                            Text(text = "Cancel")
                        }
                    },
                    text = { Text(text = "Are you sure to exit the game?") })
            }
            val onClickHome = {
                showConfirmExitDialog = true
            }
            BackHandler {
                onClickHome()
            }
            MultiplayerGameScene(
                roomId = backStackEntry.arguments!!.getString("roomId")!!.toUInt(),
                goBack = {
                    navController.popBackStack()
                },
                onClickHome = onClickHome,
                onClickGameConfig = {
                    navController.navigate("game/lobby")
                    lobbyViewModel.removeSelfRoom()
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
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
            ),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) { entry ->
            val onClickBack: () -> Unit = onClickBack@{
                if (navController.currentBackStackEntry != entry) {
                    return@onClickBack
                }
                if (navController.previousBackStackEntry?.destination?.route == "profile") {
                    navController.popBackStack("profile", true)
                } else {
                    navController.popBackStack()
                }
            }
            BackHandler {
                onClickBack()
            }
            AuthScene(
                initialIsRegister = entry.arguments?.getString("mode") == "register",
                onClickBack = onClickBack,
                onSuccess = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            "profile",
        ) {
            LoginChecker(navController)
            ProfileScene(
                remember {
                    ProfileViewModel()
                },
                onClickBack = {
                    navController.popBackStack()
                },
                onClickPlayGame = {
                    navController.navigate(
                        navController.graph["game/configuration"].id,
                        Bundle().apply {
                            putString("configuration", it)
                        })
                },
                onClickPasswordEdit = {
                    navController.navigate("profile/edit")
                },
                onSuccessfulEdit = {
                    Toast.makeText(context, "Nickname updated!", Toast.LENGTH_SHORT).show()
                }
            )
        }
        composable(
            "profile/edit",
        ) { entry ->
            EditPasswordScene(
                onClickBack = {
                    if (navController.currentBackStackEntry != entry) {
                        return@EditPasswordScene
                    } else if (navController.previousBackStackEntry?.destination?.route == "profile") {
                        navController.popBackStack("profile", false)
                    } else {
                        navController.popBackStack()
                    }
                },
                onSuccessPasswordEdit = {
                    Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
private fun LoginChecker(navController: NavHostController) {
    LaunchedEffect(true) {
        if (GlobalContext.get().get<SessionManager>().self.first() == null) {
            navController.navigate("auth/login") {
                launchSingleTop = true
            }
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
        Box(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 256.dp), contentAlignment = Alignment.Center
        ) {
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
            val isLoggedIn by remember {
                GlobalContext.get().get<SessionManager>().isLoggedIn
            }.collectAsStateWithLifecycle(null)
            // Single Player Button
            Button(
                onClick = { navController.navigate("game/configuration") },
                modifier = Modifier.width(170.dp)
            ) {
                Text("Play vs Computer", textAlign = TextAlign.Center)
            }

            var showLogInDialog by remember {
                mutableStateOf(false)
            }
            // Online log in
            if (showLogInDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showLogInDialog = false
                        navController.popBackStack()
                    },
                    confirmButton = {
                        Button(onClick = {
                            showLogInDialog = false
                            navController.navigate("auth/login") {
                                launchSingleTop = true
                            }
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showLogInDialog = false
                            navController.popBackStack()
                        }) {
                            Text(text = "Cancel")
                        }
                    },
                    text = {
                        Text(text = "Please log in to play online")
                    }
                )
            }

            // Multiplayer Button
            Button(onClick = {
                if (isLoggedIn == false) {
                    showLogInDialog = true
                } else {
                    navController.navigate("game/lobby")
                }
            }, modifier = Modifier.width(170.dp)) {
                Text("2 Players", textAlign = TextAlign.Center)
            }

            // Saved game Button
            Button(
                onClick = { navController.navigate("profile") },
                modifier = Modifier.width(170.dp)
            ) {
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

            Button(
                onClick = { navController.navigate("about") },
                modifier = Modifier.width(170.dp)
            ) {
                Text("About", textAlign = TextAlign.Center)
            }

            val context = LocalContext.current
            Button(
                onClick = { if (context is Activity) context.finish() },
                modifier = Modifier.width(170.dp)
            ) {
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


@Preview(showBackground = true)
@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun PreviewMainScreen() = ProvideCompositionalLocalsForPreview {
    MainScreen()
}
