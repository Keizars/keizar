package org.keizar.android.ui.game.sp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import org.keizar.android.ui.game.BaseGamePage
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.configuration.createBoard
import org.keizar.android.ui.game.rememberSinglePlayerGameBoardViewModel
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.utils.communication.game.Player


@Composable
fun SinglePlayerGameScene(
    startConfiguration: GameStartConfiguration,
    navController: NavController,
) {
    BaseGamePage(
        rememberSinglePlayerGameBoardViewModel(
            session = remember(startConfiguration) {
                GameSession.create(startConfiguration.createBoard())
            },
            selfPlayer = if (startConfiguration.playAs == Role.WHITE) {
                Player.FirstWhitePlayer
            } else {
                Player.FirstBlackPlayer
            },
        ),
        onClickHome = { navController.popBackStack("home", false) },
        onClickGameConfig = { navController.popBackStack("game/configuration", false) }
    )
}
