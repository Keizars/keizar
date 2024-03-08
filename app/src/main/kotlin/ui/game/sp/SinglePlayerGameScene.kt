package org.keizar.android.ui.game.sp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.game.BaseGamePage
import org.keizar.android.ui.game.actions.UndoButton
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.rememberSinglePlayerGameBoardViewModel
import org.keizar.game.Difficulty
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.game.snapshot.buildGameSession
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
        vm.launchInBackground(start = CoroutineStart.UNDISPATCHED) {
            removeSavedState()
            withContext(Dispatchers.Main) {
                navController.popBackStack("home", false)
            }
        }
    }

    if (isSystemInLandscape()) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
            Box {
                BaseGamePage(
                    vm,
                    onClickHome = onClickHome,
                    onClickGameConfig = onClickGameConfig,
                    onClickLogin = onClickLogin,
                    actions = {
                        UndoButton(vm = vm)
                    },
                    modifier = Modifier.width(IntrinsicSize.Min)
                )
            }
//            Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
//                UndoButton(vm = vm)
//            }
        }
    } else {
        BaseGamePage(
            vm,
            onClickHome = onClickHome,
            onClickGameConfig = onClickGameConfig,
            onClickLogin = onClickLogin,
            actions = {
                UndoButton(vm = vm)
            }
        )
    }
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
            onClickGameConfig = { }
        )
    }
}


