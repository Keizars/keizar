package org.keizar.android.ui.tutorial

import androidx.compose.runtime.Stable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import org.keizar.android.tutorial.Tutorial
import org.keizar.android.tutorial.TutorialSession
import org.keizar.android.tutorial.newSession
import org.keizar.android.ui.game.BaseGameBoardViewModel
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.game.GameSession
import org.keizar.utils.communication.game.Player

fun TutorialGameBoardViewModel(
    tutorial: Tutorial,
): TutorialGameBoardViewModel {
    val session = tutorial.newSession(
        // TODO: coroutine context for newSession
    )

    return TutorialGameBoardViewModel(
        tutorialSession = session,
    )
}

class TutorialGameBoardViewModel(
    @Stable
    val tutorialSession: TutorialSession,
    game: GameSession = tutorialSession.game, selfPlayer: Player = tutorialSession.tutorial.player,
) : BaseGameBoardViewModel(game, selfPlayer) {
    override val startConfiguration: GameStartConfiguration = GameStartConfiguration.random()

    init {
        tutorialSession.requests.requestingShowPossibleMoves
            .transformLatest { request ->
                if (request == null) {
                    return@transformLatest
                }
                coroutineScope {
                    startPick(request.logicalPos)
                    launch {
                        delay(request.duration)
                        completePick(false)
                        request.respond()
                    }
                }
                emit(Unit)
            }.launchIn(backgroundScope)
    }
}
