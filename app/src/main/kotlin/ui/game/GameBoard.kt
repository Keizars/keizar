package org.keizar.android.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
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
            } else {
                delay(2000)
            }
        }

        when (winner) {
            null -> {}
            Role.WHITE -> {
                if (showDialogWhiteWin) {
                    AlertDialog(onDismissRequest = {},
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
                    AlertDialog(onDismissRequest = {},
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

    // A row of tokens
    Row(

        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Create a token for each number
        (1..3).forEach { number ->
            Token(
                number = number,
                isActive = number == winningCounter
            )

        }
    }
}

@Composable
fun Token(number: Int, isActive: Boolean) {
    Canvas(modifier = Modifier.size(48.dp).padding(8.dp)) {
        val circleColor = if (isActive) Color.Gray else Color.LightGray
        val radius = size.minDimension / 2
        val center = Offset(radius, radius)

        // Draw the token
        drawCircle(
            circleColor,
            radius,
            center
        )

        // Draw the dots
        if (isActive) {
           val dotColor = Color.White
            // Calculate positions for dots based on 'number'
            val dotPositions = getDotPositions(number, center, radius / 3)
            dotPositions.forEach { pos ->
                drawCircle(dotColor, radius / 6, pos)
            }
        }
    }
}

fun getDotPositions(number: Int, center: Offset, distance: Float): List<Offset> {
    return when (number) {
        1 -> listOf(
            center // One dot in the center
        )
        2 -> listOf(
            Offset(center.x - distance / 2, center.y), // One dot to the left of center
            Offset(center.x + distance / 2, center.y) // One dot to the right of center
        )
        3 -> listOf(
            Offset(center.x - distance, center.y), // One dot to the left of center
            center, // One dot in the center
            Offset(center.x + distance, center.y) // One dot to the right of center
        )
        else -> emptyList() // No dots if the number is not 1, 2, or 3
    }
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