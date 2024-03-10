package org.keizar.android.ui.game.sp

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.game.BaseGamePage
import org.keizar.android.ui.game.actions.UndoButton
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.rememberSinglePlayerGameBoardViewModel
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.game.snapshot.buildGameSession
import org.keizar.utils.communication.game.Difficulty
import org.keizar.utils.communication.game.Player


@Composable
fun SinglePlayerGameScene(
    startConfiguration: GameStartConfiguration,
    session: GameSession,
    navController: NavController,
) {
    val vm = rememberSinglePlayerGameBoardViewModel(
        session = session,
        selfPlayer = if (startConfiguration.playAs == Role.WHITE) {
            Player.FirstWhitePlayer
        } else {
            Player.FirstBlackPlayer
        },
        difficulty = startConfiguration.difficulty,
    )

    var showConfirmExitDialog by remember { mutableStateOf(false) }
    if (showConfirmExitDialog) {
        AlertDialog(onDismissRequest = { showConfirmExitDialog = false },
            confirmButton = {
                Button(onClick = {
                    showConfirmExitDialog = false

                    vm.launchInBackground(start = CoroutineStart.UNDISPATCHED) {
                        removeSavedState()
                        withContext(Dispatchers.Main) {
                            navController.popBackStack("home", false)
                        }
                    }
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

    
    val onClickLogin = {
        navController.navigate("auth/login") {
            launchSingleTop = true
        }
    }
    val onClickGameConfig = {
        navController.navigate("game/configuration") {
            launchSingleTop = true
        }
    }
    val onClickHome: () -> Unit = {
        showConfirmExitDialog = true
    }
    BackHandler {
        onClickHome()
    }

    BaseGamePage(
        vm,
        onClickHome = onClickHome,
        onClickNewGame = onClickGameConfig,
        onClickLogin = onClickLogin,
        actions = {
            UndoButton(vm = vm)
        }
    )
}


@Preview
@Preview(fontScale = 2f)
@Preview(device = Devices.TABLET)
@Composable
private fun TestEndGameWhenNoPiecesCanMove() {
    ProvideCompositionalLocalsForPreview {
        BaseGamePage(
            rememberSinglePlayerGameBoardViewModel(
                session = remember {
                    buildGameSession {
                        round {
                            resetPieces {
                                val c = 'a'
                                black("${c}8")
                                black("${c}7")
                                white("${c}6")
                                white("${c}4")
                            }
                        }
                        round { }
                    }
                },
                selfPlayer = Player.FirstWhitePlayer,
                difficulty = Difficulty.EASY,
            ),
            onClickHome = { },
            onClickNewGame = { }
        )
    }
}


