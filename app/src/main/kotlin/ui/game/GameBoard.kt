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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
        selfPlayer = Player.Player1,
    )
    Column(modifier = Modifier) {
        val winner = vm.winner.collectAsState().value
        val finalWinner = vm.finalWinner.collectAsState().value

        WinningCounter(vm)

        CapturedPieces(vm, Role.BLACK)

        Box(modifier = modifier) {
            BoardBackground(vm)
            BoardPieces(vm)
            PossibleMovesOverlay(vm)
        }
        CapturedPieces(vm, Role.WHITE)

        when (finalWinner) {
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
            null -> {
                when (winner) {
                    null -> {
                        // do nothing
                    }
                    Role.WHITE -> {
                        AlertDialog(onDismissRequest = {},
                            title = { Text(text = "Game Over, White wins!") },
                            confirmButton = {
                                Button(onClick = { vm.startNextRound(vm.selfPlayer) }) {
                                    Text(text = "Start the next round")
                                }
                            })
                    }
                    Role.BLACK -> {
                        AlertDialog(onDismissRequest = {},
                            title = { Text(text = "Game Over, Black wins!") },
                            confirmButton = {
                                Button(onClick = { vm.startNextRound(vm.selfPlayer) }) {
                                    Text(text = "Start the next round")
                                }
                            })
                    }
                }
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
            Text(text = "White Captured Pieces:")
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
            Text(text = "Black Captured Pieces:")
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