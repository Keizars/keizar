package org.keizar.android.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.game.actions.GameBoardTopBar
import org.keizar.android.ui.game.actions.GameOverDialog
import org.keizar.android.ui.game.actions.LandscapeBottomBar
import org.keizar.android.ui.game.actions.RoundOneBottomBar
import org.keizar.android.ui.game.actions.RoundTwoBottomBar
import org.keizar.android.ui.game.actions.WinningRoundDialog
import org.keizar.android.ui.game.transition.CapturedPiecesHost

/**
 * The game board, consisting of the following layers (from bottom to top):
 * - the background tiles
 * - [BoardPieces]
 * - [PossibleMovesOverlay]
 * - [boardOverlay]
 */
@Composable
fun GameBoard(
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier,
    opponentCapturedPieces: @Composable RowScope.(tileSize: DpSize, sourceCoordinates: LayoutCoordinates) -> Unit = { tileSize, sourceCoordinates ->
        CapturedPiecesHost(
            capturedPieceHostState = vm.theirCapturedPieceHostState,
            slotSize = tileSize,
            sourceCoordinates = sourceCoordinates,
        )
    },
    myCapturedPieces: @Composable RowScope.(tileSize: DpSize, sourceCoordinates: LayoutCoordinates) -> Unit = { tileSize, sourceCoordinates ->
        CapturedPiecesHost(
            capturedPieceHostState = vm.myCapturedPieceHostState,
            slotSize = tileSize,
            sourceCoordinates = sourceCoordinates,
        )
    },
    boardOverlay: @Composable BoxScope.() -> Unit = { },
) {
    var boardGlobalCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
    val tileSize by vm.pieceArranger.tileSize.collectAsStateWithLifecycle(DpSize.Zero)
//    Row(
//        modifier.fillMaxWidth().padding(16.dp).height(48.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = modifier.weight(1f).height(48.dp)
//        ) {
//            // TODO: Avatar
//        }
//        Text(
//            text = "Opponent Username",
//            modifier.weight(3f),
//            fontSize = 18.sp
//        )
//    }

    Column {
        Row(Modifier.align(Alignment.Start), verticalAlignment = CenterVertically) {
            boardGlobalCoordinates?.let {
                opponentCapturedPieces(tileSize, it)
            }
        }

        Box(modifier = modifier.onGloballyPositioned { boardGlobalCoordinates = it }) {
            BoardBackground(vm, Modifier.matchParentSize())
            BoardPieces(vm)
            PossibleMovesOverlay(vm)
            boardOverlay()
        }

        Row(Modifier.align(Alignment.End), verticalAlignment = CenterVertically) {
            boardGlobalCoordinates?.let {
                myCapturedPieces(tileSize, it)
            }
        }
    }
}

@Composable
fun GameBoardScaffold(
    vm: GameBoardViewModel,
    board: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = { GameBoardTopBar(vm) },
    bottomBar: @Composable () -> Unit = { },
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth()) {
                topBar()
            }

            board()

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End
            ) {
                actions()
            }
            if (!isSystemInLandscape()) {
                Row { bottomBar() }
            }
        }

        if (isSystemInLandscape()) {
            // Landscape mode: actions float above the board
            LandscapeBottomBar(
                Modifier
                    .align(BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                bottomBar()
            }
        }
    }
}

@Composable
fun DialogsAndBottomBar(
    vm: GameBoardViewModel,
    onClickHome: () -> Unit,
    onClickGameConfig: () -> Unit,
    onClickLogin: () -> Unit
) {
    val winner by vm.winner.collectAsState()
    val finalWinner by vm.finalWinner.collectAsState()
    val showRoundOneBottomBar =
        (winner != null && vm.currentRoundCount.collectAsState().value == 0)

    val showRoundTwoBottomBar =
        (winner != null && vm.currentRoundCount.collectAsState().value == 1)

    val playingTransition = vm.boardTransitionController.isPlayingTransition.collectAsState().value

    if (!playingTransition) {
        WinningRoundDialog(winner, vm)
        GameOverDialog(vm, finalWinner, onClickHome)

        if (showRoundOneBottomBar) {
            Column {
                RoundOneBottomBar(
                    vm, onClickHome,
                    if (isSystemInLandscape()) Modifier else Modifier.fillMaxWidth()
                )
            }
        }

        if (showRoundTwoBottomBar) {
            Column {
                RoundTwoBottomBar(
                    vm, onClickHome, onClickGameConfig, onClickLogin,
                    if (isSystemInLandscape()) Modifier else Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun PreviewGameBoard() {
    val vm = rememberSinglePlayerGameBoardForPreview()
    GameBoardScaffold(
        vm,
        board = {
            GameBoard(vm, Modifier.size(400.dp))
        }
    )
}
