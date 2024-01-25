package org.keizar.android.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.configuration.createBoard
import org.keizar.game.Difficulty
import org.keizar.game.GameResult
import org.keizar.game.GameSession
import org.keizar.game.Player
import org.keizar.game.Role


@Composable
fun GameBoard(
    startConfiguration: GameStartConfiguration,
    modifier: Modifier = Modifier,
    onClickHome: () -> Unit,
) {
    val vm = rememberGameBoardViewModel(
        GameSession.create(startConfiguration.createBoard()),
        selfPlayer = if (startConfiguration.playAs == Role.WHITE) Player.Player1 else Player.Player2
    )
    Column(modifier = Modifier) {
        val winner = vm.winner.collectAsState().value
        val finalWinner = vm.finalWinner.collectAsState().value
        val selfRole = vm.selfRole.collectAsState().value
        var showDialogWhiteWin by remember { mutableStateOf(false) }
        var showDialogBlackWin by remember { mutableStateOf(false) }

        WinningCounter(vm)

        CapturedPieces(vm, selfRole)


        Box(modifier = modifier) {
            BoardBackground(vm)
            BoardPieces(vm)
            PossibleMovesOverlay(vm)
        }

        CapturedPieces(vm, selfRole.other())

        LaunchedEffect(winner) {
            if (winner == null) {
                // Reset the state at the start of a new round or when there is no winner
                showDialogWhiteWin = true
                showDialogBlackWin = true
            }
        }

        when (winner) {
            null -> {}
            Role.WHITE -> {
                if (showDialogWhiteWin) {
                    AlertDialog(onDismissRequest = {showDialogWhiteWin = false},
                        title = { Text(text = "This round is over, White wins!") },
                        confirmButton = {
                            Button(onClick = {
                                vm.startNextRound(vm.selfPlayer)
                                showDialogWhiteWin = false
                            }) {
                                Text(text = "Confirm")
                            }
                        })
                }
            }
            Role.BLACK -> {
                if (showDialogBlackWin) {
                    AlertDialog(onDismissRequest = {showDialogBlackWin = false},
                        title = { Text(text = "This round is over, Black wins!") },
                        confirmButton = {
                            Button(onClick = {
                                vm.startNextRound(vm.selfPlayer)
                                showDialogBlackWin = false
                            }) {
                                Text(text = "Confirm")
                            }
                        })
                }
            }
        }

        when (finalWinner) {
            null -> {
                // do nothing
            }
            is GameResult.Draw -> {
                AlertDialog(onDismissRequest = {},
                    title = { Text(text = "Game Over, Draw") },
                    confirmButton = {
                        Button(onClick = onClickHome) {
                            Text(text = "Back to main page")
                        }
                    })
            }

            is GameResult.Winner -> {
                AlertDialog(onDismissRequest = {},
                    title = { Text(text = "Game Over, ${finalWinner.player} wins!") },
                    confirmButton = {
                        Button(onClick = onClickHome) {
                            Text(text = "Back to main page")
                        }
                    })
            }
        }
    }
}

@Composable
fun CapturedPieces(vm: GameBoardViewModel, role: Role) {
    val capturedPieces by if (role == Role.WHITE) {
        vm.whiteCapturedPieces.collectAsState()
    } else {
        vm.blackCapturedPieces.collectAsState()
    }
    if (role == Role.WHITE) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Captured White Pieces:")
            for (i in 0 until capturedPieces) {
                PlayerIcon(
                    color = Color.White,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Captured Black Pieces:")
            for (i in 0 until capturedPieces) {
                PlayerIcon(
                    color = Color.Black,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun WinningCounter(vm: GameBoardViewModel) {
    val winningCounter by vm.winningCounter.collectAsState()
    Text(text = "Winning Keizar Counter: $winningCounter")
}


@Preview(showBackground = true)
@Composable
private fun PreviewGameBoard() {
    BoxWithConstraints {
        GameBoard(
            remember {
                GameStartConfiguration(
                    layoutSeed = 0,
                    playAs = Role.WHITE,
                    difficulty = Difficulty.EASY,
                )
            },
            Modifier.size(min(maxWidth, maxHeight)),
            onClickHome = { /* Navigate to home page*/ },
        )
    }
}