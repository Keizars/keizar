package org.keizar.android.ui.game

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.delay
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.configuration.createBoard
import org.keizar.game.Difficulty
import org.keizar.game.GameResult
import org.keizar.game.GameSession
import org.keizar.game.Player
import org.keizar.game.Role
import kotlin.math.sqrt


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
        val showRoundOneBottomBar =
            (winner != null && vm.currentRoundCount.collectAsState().value == 0)

        val showRoundTwoBottomBar =
            (winner != null && vm.currentRoundCount.collectAsState().value == 1)

        WinningCounter(vm)

        CapturedPieces(vm, selfRole)


        Box(modifier = modifier) {
            BoardBackground(vm, Modifier.matchParentSize())
            BoardPieces(vm)
            PossibleMovesOverlay(vm)
        }

        CapturedPieces(vm, selfRole.other())

        WinningRoundDialog(winner, vm)

        GameOverDialog(vm, finalWinner, onClickHome)

        if (showRoundOneBottomBar) {
            RoundOneBottomBar(vm, onClickHome)
        }

        if (showRoundTwoBottomBar) {
            RoundTwoBottomBar(vm)
        }
    }
}

@Composable
fun RoundOneBottomBar(vm: GameBoardViewModel, onClickHome: () -> Unit) {
    // Calculate the minimum width for a button based on the longest text
    // This is just an example, you may want to adjust it based on your UI needs
    val buttonWidth = 160.dp

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // All buttons now have a fixed width
        Button(
            onClick = onClickHome,
            modifier = Modifier
                .width(buttonWidth)
                .padding(4.dp)
        ) {
            Text(text = "Home", textAlign = TextAlign.Center)
        }

        Button(
            onClick = {/* TODO */ },
            modifier = Modifier
                .width(buttonWidth)
                .padding(4.dp)
        ) {
            Text(text = "Replay", textAlign = TextAlign.Center)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var showDialog by remember { mutableStateOf(false) }
        val showResults = remember { mutableStateOf(true) }
        Button(
            onClick = {
                showDialog = true
                showResults.value = true
            },
            modifier = Modifier
                .width(buttonWidth)
                .padding(4.dp)
        ) {
            Text(text = "Results", textAlign = TextAlign.Center)
        }

        if (showDialog) {
            WinningRoundDialog(winner = vm.winner.collectAsState().value, vm, showResults)
        }

        Button(
            onClick = { vm.startNextRound(vm.selfPlayer) },
            modifier = Modifier
                .width(buttonWidth)
                .padding(4.dp)
        ) {
            Text(text = "Next Round", textAlign = TextAlign.Center)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current
        Button(
            onClick = { if (context is Activity) context.finish() },
            modifier = Modifier
                .width(buttonWidth)
                .padding(4.dp)
        ) {
            Text(text = "Exit", textAlign = TextAlign.Center)
        }
    }
}


@Composable
fun RoundTwoBottomBar(vm: GameBoardViewModel) {

}

@Composable
fun GameOverDialog(vm: GameBoardViewModel, finalWinner: GameResult?, onClickHome: () -> Unit) {
    when (finalWinner) {
        null -> {
            // do nothing
        }

        is GameResult.Draw -> {
            AlertDialog(onDismissRequest = {},
                title = { Text(text = "Game Over, Draw") },
                text = {
                    Text(
                        text = "Round 1:\n" +
                                "   Winner: ${vm.round1Winner.collectAsState(null)}\n" +
                                "   White captured: ${vm.getRoundPieceCount(0, Role.WHITE)}\n" +
                                "   Black captured: ${vm.getRoundPieceCount(0, Role.BLACK)}\n" +
                                "Round 2 Winner: ${vm.round2Winner.collectAsState(null)}\n" +
                                "   White captured: ${vm.getRoundPieceCount(1, Role.WHITE)}\n" +
                                "   Black captured: ${vm.getRoundPieceCount(1, Role.BLACK)}"
                    )
                },
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

@Composable
fun WinningRoundDialog(
    winner: Role?,
    vm: GameBoardViewModel,
    showFlag: MutableState<Boolean> = mutableStateOf(false)
) {
    var showDialogWhiteWin by remember { mutableStateOf(false) }
    var showDialogBlackWin by remember { mutableStateOf(false) }

    val whiteCapturedPieces by vm.whiteCapturedPieces.collectAsState()
    val blackCapturedPieces by vm.blackCapturedPieces.collectAsState()
    val currentRoundCount by vm.currentRoundCount.collectAsState()

    if (showFlag.value) {
        if (winner == Role.WHITE) {
            showDialogWhiteWin = true
        } else if (winner == Role.BLACK) {
            showDialogBlackWin = true
        }
    }

    when (winner) {
        null -> {
            showDialogWhiteWin = true
            showDialogBlackWin = true
        }

        Role.WHITE -> {
            if (showDialogWhiteWin) {
                AlertDialog(onDismissRequest = {},
                    title = { Text(text = "This Round Winner: White") },
                    text = {
                        Text(
                            text = "White captured: ${whiteCapturedPieces}\n" +
                                    "Black captured: ${blackCapturedPieces}\n"
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (currentRoundCount == 1) {
                                vm.startNextRound(vm.selfPlayer)
                            }
                            showDialogWhiteWin = false
                            showFlag.value = false
                        }) {
                            Text(text = "OK")
                        }
                    })
            }
        }

        Role.BLACK -> {
            if (showDialogBlackWin) {
                AlertDialog(onDismissRequest = {},
                    title = { Text(text = "This Round Winner: Black") },
                    text = {
                        Text(
                            text = "White captured: ${whiteCapturedPieces}\n" +
                                    "Black captured: ${blackCapturedPieces}\n"
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (currentRoundCount == 1) {
                                vm.startNextRound(vm.selfPlayer)
                            }
                            showDialogBlackWin = false
                            showFlag.value = false
                        }) {
                            Text(text = "OK")
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
    val flippedStates = remember { mutableStateListOf(true, true, true) }
    var firstTimeFlipped by remember { mutableStateOf(false) }

    // Whenever winningCounter updates, set the corresponding token state to flipped
    LaunchedEffect(winningCounter) {
        if (winningCounter in 1..3) {
            flippedStates[winningCounter - 1] = true
        } else {
            if (!firstTimeFlipped) {
                delay(3000)
                firstTimeFlipped = true
            }
            flippedStates.forEachIndexed { index, _ -> flippedStates[index] = false }
        }
    }

    // A row of tokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Create a token for each number
        (1..3).forEach { number ->
            Token(
                number = number,
                isFlipped = flippedStates[number - 1]
            )

        }
    }
}

@Composable
fun Token(number: Int, isFlipped: Boolean) {
    val rotationDegrees by animateFloatAsState(targetValue = if (isFlipped) 180f else 0f)
    Canvas(modifier = Modifier
        .size(48.dp)
        .padding(8.dp)
        .graphicsLayer {
            rotationY = rotationDegrees
            cameraDistance = 12f * density
        }) {
        val circleColor = if (rotationDegrees <= 90f) Color.Gray else Color.LightGray
        val radius = size.minDimension / 2
        val center = Offset(radius, radius)

        // Draw the token
        drawCircle(
            circleColor,
            radius,
            center
        )
        if (rotationDegrees > 90f) {
            val dotColor = Color.Black
            // Calculate positions for dots based on 'number'
            val dotPositions = getDotPositions(number, center, radius / 2)
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
            Offset(center.x - distance / 3, center.y + distance / 2),
            Offset(center.x + distance / 3, center.y - distance / 2)
        )

        3 -> {
            val sideLength = distance * 2 / sqrt(3f)
            val horizontalDistance = sideLength / 2
            listOf(
                Offset(center.x, center.y - distance / 2), // Top vertex
                Offset(
                    center.x - horizontalDistance,
                    center.y + distance / 2
                ), // Bottom left vertex
                Offset(
                    center.x + horizontalDistance,
                    center.y + distance / 2
                )  // Bottom right vertex
            )
        }

        else -> emptyList()
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