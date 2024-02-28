package org.keizar.android.ui.game

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.flatMapLatest
import org.keizar.android.ui.game.transition.CapturedPiecesHost
import org.keizar.game.Role
import org.keizar.utils.communication.game.GameResult
import org.keizar.utils.communication.game.Player
import kotlin.math.sqrt


@Composable
fun GameBoardTopBar(
    vm: GameBoardViewModel,
    turnStatusIndicator: (@Composable () -> Unit)? = {
        TurnStatusIndicator(vm, Modifier.padding(all = 6.dp))
    },
    winningCounter: (@Composable () -> Unit)? = {
        WinningCounter(vm)
    },
) {
    Box(Modifier.fillMaxWidth()) {
        turnStatusIndicator?.let { it ->
            Box(modifier = Modifier.padding(all = 6.dp)) {
                it()
            }
        }
        winningCounter?.let {
            Box(modifier = Modifier.align(Alignment.Center)) {
                it()
            }
        }
    }
}

@Composable
fun GameBoard(
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier,
    boardOverlay: @Composable BoxScope.() -> Unit = { },
) {
    var boardGlobalCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }

    val tileSize by vm.pieceArranger.tileSize.collectAsStateWithLifecycle(DpSize.Zero)

    CapturedPiecesHost(
        capturedPieceHostState = vm.theirCapturedPieceHostState,
        slotSize = tileSize,
        sourceCoordinates = boardGlobalCoordinates,
        Modifier.fillMaxWidth()
    )

    Box(modifier = modifier.onGloballyPositioned { boardGlobalCoordinates = it }) {
        BoardBackground(vm, Modifier.matchParentSize())
        BoardPieces(vm)
        PossibleMovesOverlay(vm)
        boardOverlay()
    }

    CapturedPiecesHost(
        capturedPieceHostState = vm.myCapturedPieceHostState,
        slotSize = tileSize,
        sourceCoordinates = boardGlobalCoordinates,
        Modifier.fillMaxWidth()
    )
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
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
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

        Row {
            bottomBar()
        }
    }
}

@Composable
fun GameBoardScaffoldLandscape(
    vm: GameBoardViewModel,
    board: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = { GameBoardTopBar(vm) },
    bottomBar: @Composable () -> Unit = { },
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(modifier = modifier) {
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

        Row {
            bottomBar()
        }
    }
}

@Composable
fun DialogsAndBottomBar(
    vm: GameBoardViewModel,
    onClickHome: () -> Unit,
    onClickGameConfig: () -> Unit
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
                RoundOneBottomBar(vm, onClickHome)
            }
        }

        if (showRoundTwoBottomBar) {
            Column {
                RoundTwoBottomBar(vm, onClickHome, onClickGameConfig)
            }
        }
    }
}

@Composable
fun RoundOneBottomBar(vm: GameBoardViewModel, onClickHome: () -> Unit) {
    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        ActionButton(
            onClick = onClickHome,
            icon = { Icon(Icons.Default.Home, null) },
            text = { Text("Home", fontSize = 10.sp) })

        if (vm.singlePlayerMode) {
            ActionButton(
                onClick = { vm.replayCurrentRound() },
                icon = { Icon(Icons.Default.Replay, null) },
                text = { Text(text = "Replay", fontSize = 10.sp) })
        }

        var showDialog by remember { mutableStateOf(false) }
        val showResults = remember { mutableStateOf(true) }
        ActionButton(
            onClick = {
                showDialog = true
                showResults.value = true
            },
            icon = { Icon(Icons.Default.Assessment, null) },
            text = { Text(text = "Results", fontSize = 10.sp) })

        if (showDialog) {
            WinningRoundDialog(winner = vm.winner.collectAsState().value, vm, showResults)
        }

        val context = LocalContext.current
        ActionButton(
            onClick = { if (context is Activity) context.finish() },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) },
            text = { Text("Exit", fontSize = 10.sp) })

        ActionButton(
            onClick = {
                vm.startNextRound(vm.selfPlayer)
                vm.setEndRoundAnnouncement(false)
            },
            icon = { Icon(Icons.Default.SkipNext, null) },
            text = { Text("Next Round", fontSize = 10.sp) }
        )

    }

}

@Composable
fun RoundTwoBottomBar(
    vm: GameBoardViewModel,
    onClickHome: () -> Unit,
    onClickGameConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        Row(
            modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            ActionButton(
                onClick = onClickHome,
                icon = { Icon(Icons.Default.Home, null) },
                text = { Text("Home", fontSize = 10.sp) })

            if (vm.singlePlayerMode) {
                ActionButton(
                    onClick = {
                        vm.replayCurrentRound()
                        vm.setGameOverReadyToBeAnnouncement(false)
                    },

                    icon = { Icon(Icons.Default.Replay, null) },
                    text = { Text(text = "Replay Round", fontSize = 10.sp) })


                ActionButton(
                    onClick = {
                        vm.replayGame()
                        vm.setGameOverReadyToBeAnnouncement(false)
                    },

                    icon = { Icon(Icons.Default.Replay10, null) },
                    text = { Text(text = "Replay Game", fontSize = 10.sp) })
            }

            ActionButton(
                onClick = { /*TODO*/ },
                icon = { Icon(Icons.Default.Save, null) },
                text = { Text(text = "Save Game", fontSize = 10.sp) })

            ActionButton(
                onClick = { vm.setShowGameOverResults(true) },
                icon = { Icon(Icons.Default.Assessment, null) },
                text = {
                    Text(
                        text = "Results",
                        fontSize = 10.sp
                    )
                })
        }

        Row(
            modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            val context = LocalContext.current
            Button(
                onClick = { if (context is Activity) context.finish() },
                colors = ButtonDefaults.buttonColors(
                    Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                Text(text = "Exit", textAlign = TextAlign.Center)
            }

            Button(
                onClick = { onClickGameConfig() },
            ) {
                Text(text = "New Game", textAlign = TextAlign.Center)
            }

        }
    }
}


@Composable
fun GameOverDialog(vm: GameBoardViewModel, finalWinner: GameResult?, onClickHome: () -> Unit) {
    val gameOverReadyToBeAnnounced by vm.gameOverReadyToBeAnnounced.collectAsState()
    val showGameOverResults by vm.showGameOverResults.collectAsState()
    if (gameOverReadyToBeAnnounced || showGameOverResults) {
        when (finalWinner) {
            null -> {
                // do nothing
            }

            is GameResult.Draw -> {
                val playerStatistics = vm.getPlayerStatistics(vm.selfPlayer)

                AlertDialog(onDismissRequest = {},
                    title = {
                        Text(
                            text = "Game Over\nDraw",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            text = "You captured: ${playerStatistics.round1Captured} + ${playerStatistics.round2Captured} = ${playerStatistics.round1Captured + playerStatistics.round2Captured}\n" +
                                    "Opponent captured:  ${playerStatistics.round1OpponentCaptured} + ${playerStatistics.round2OpponentCaptured} = ${playerStatistics.round1OpponentCaptured + playerStatistics.round2OpponentCaptured}\n" +
                                    "Number of moves: ${playerStatistics.round1Moves} + ${playerStatistics.round2Moves} = ${playerStatistics.round1Moves + playerStatistics.round2Moves}\n" +
                                    "Time: ${playerStatistics.round1Time / 60} m ${playerStatistics.round1Time % 60} s + ${playerStatistics.round2Time / 60} m ${playerStatistics.round2Time % 60} s = ${(playerStatistics.round1Time + playerStatistics.round2Time) / 60} m ${(playerStatistics.round1Time + playerStatistics.round2Time) % 60} s\n" +
                                    "Your moves' average time: ${playerStatistics.round1AverageTime / 60} m ${playerStatistics.round1AverageTime % 60} s + ${playerStatistics.round2AverageTime / 60} m ${playerStatistics.round2AverageTime % 60} s = ${(playerStatistics.round1AverageTime + playerStatistics.round2AverageTime) / 60} m ${(playerStatistics.round1AverageTime + playerStatistics.round2AverageTime) % 60} s\n",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            vm.setGameOverReadyToBeAnnouncement(false)
                            vm.setShowGameOverResults(false)
                        }) {
                            Text(text = "OK")
                        }
                    })
            }


            is GameResult.Winner -> {
                val playerStatistics = vm.getPlayerStatistics(vm.selfPlayer)
                val winnerText = if (finalWinner.player == vm.selfPlayer) "You Win" else "You Lose"
                AlertDialog(onDismissRequest = {},
                    title = {
                        Text(
                            text = "Game Over\n$winnerText",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = "You captured: ${playerStatistics.round1Captured} + ${playerStatistics.round2Captured} = ${playerStatistics.round1Captured + playerStatistics.round2Captured}\n" +
                                    "Opponent captured:  ${playerStatistics.round1OpponentCaptured} + ${playerStatistics.round2OpponentCaptured} = ${playerStatistics.round1OpponentCaptured + playerStatistics.round2OpponentCaptured}\n" +
                                    "Number of moves: ${playerStatistics.round1Moves} + ${playerStatistics.round2Moves} = ${playerStatistics.round1Moves + playerStatistics.round2Moves}\n" +
                                    "Time: ${playerStatistics.round1Time / 60} m ${playerStatistics.round1Time % 60} s + ${playerStatistics.round2Time / 60} m ${playerStatistics.round2Time % 60} s = ${(playerStatistics.round1Time + playerStatistics.round2Time) / 60} m ${(playerStatistics.round1Time + playerStatistics.round2Time) % 60} s\n" +
                                    "Your moves' average time: ${playerStatistics.round1AverageTime / 60} m ${playerStatistics.round1AverageTime % 60} s + ${playerStatistics.round2AverageTime / 60} m ${playerStatistics.round2AverageTime % 60} s = ${(playerStatistics.round1AverageTime + playerStatistics.round2AverageTime) / 60} m ${(playerStatistics.round1AverageTime + playerStatistics.round2AverageTime) % 60} s\n",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            vm.setGameOverReadyToBeAnnouncement(false)
                            vm.setShowGameOverResults(false)
                        }) {
                            Text(text = "Ok")
                        }
                    })

            }
        }
    }
}

@Composable
fun WinningRoundDialog(
    winner: Role?,
    vm: GameBoardViewModel,
    showFlag: MutableState<Boolean> = mutableStateOf(false)
) {
    val currentRoundCount by vm.currentRoundCount.collectAsState()
    val endRoundAnnounced by vm.endRoundAnnounced.collectAsState()

    when (winner) {
        null -> {}

        else -> {
            val playerStatistics = vm.getPlayerStatistics(vm.selfPlayer)
            val timeTaken: Int
            val numOfMoves: Int
            val selfCapturedPieces: Int
            val opponentCapturedPieces: Int
            val averageTimeTaken: Long

            if (currentRoundCount == 0) {
                timeTaken = playerStatistics.round1Time
                numOfMoves = playerStatistics.round1Moves
                selfCapturedPieces = playerStatistics.round1Captured
                opponentCapturedPieces = playerStatistics.round1OpponentCaptured
                averageTimeTaken = playerStatistics.round1AverageTime
            } else {
                timeTaken = playerStatistics.round2Time
                numOfMoves = playerStatistics.round2Moves
                selfCapturedPieces = playerStatistics.round2Captured
                opponentCapturedPieces = playerStatistics.round2OpponentCaptured
                averageTimeTaken = playerStatistics.round2AverageTime
            }

            val roundCountText = if (currentRoundCount == 0) "First Round" else "Second Round"
            val winningStatusText =
                if (winner == vm.selfRole.collectAsState().value) "You Win" else "You Lose"
            if (showFlag.value || !endRoundAnnounced) {
                AlertDialog(onDismissRequest = {},
                    title = {
                        Text(
                            text = "$roundCountText \n" +
                                    "$winningStatusText\n",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            text = "You captured: $selfCapturedPieces\n" +
                                    "Opponent captured: $opponentCapturedPieces\n" +
                                    "Number of moves: $numOfMoves\n" +
                                    "Time: ${timeTaken / 60} m ${timeTaken % 60} \n" +
                                    "Your moves' average time: ${averageTimeTaken / 60} m ${averageTimeTaken % 60} s",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (currentRoundCount == 1) {
                                vm.startNextRound(vm.selfPlayer)
                                vm.setGameOverReadyToBeAnnouncement(true)
                            }
                            vm.setEndRoundAnnouncement(true)
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
fun WinningCounter(
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier
) {
    val winningCounter by vm.winningCounter.collectAsState()
    val flippedStates = remember { mutableStateListOf(true, true, true) }

    // Whenever winningCounter updates, set the corresponding token state to flipped
    LaunchedEffect(winningCounter) {
        if (winningCounter in 1..3) {
            flippedStates[winningCounter - 1] = false
        } else {
            for (i in 0 until 3) {
                flippedStates[i] = true
            }
        }
    }

    // A row of tokens
    Row(
        modifier = modifier,
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
    val paleGreen = Color(0xFFC8E6C9)
    val paleRed = Color(0xFFEB8C8C)
    Canvas(modifier = Modifier
        .size(48.dp)
        .padding(8.dp)
        .graphicsLayer {
            rotationY = rotationDegrees
            cameraDistance = 12f * density
        }) {
        val circleColor = if (rotationDegrees <= 90f) paleRed else paleGreen
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

@Composable
fun TurnStatusIndicator(
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier
) {
    val selfRole by vm.selfRole.collectAsState()
    val curRole by vm.currentRound.flatMapLatest { it.curRole }.collectAsState(initial = Role.WHITE)
    val isPlayerTurn = selfRole == curRole


    // Animate the rotation
    val rotationY by animateFloatAsState(
        targetValue = if (isPlayerTurn) 0f else 180f,
        animationSpec = tween(durationMillis = 600), label = ""
    )


    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(90.dp)
            .height(36.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 8 * density
            }
            .background(
                color = if (rotationY <= 90f) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        if (rotationY <= 90f) {
            Text(
                text = "Your Turn",
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(
                text = "Waiting",
                color = Color.White,
                modifier = Modifier.graphicsLayer { this.rotationY = 180f }
            )
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

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    isLoading: Boolean = false
) {
    Column(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(
                remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = onClick
            )
            .height(64.dp)
            .padding(horizontal = 4.dp)
            .padding(all = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        icon()
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                text()
            }

            AnimatedVisibility(isLoading) {
                Box(
                    Modifier
                        .padding(start = 8.dp)
                        .height(12.dp), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 2.dp)
                }
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

@Composable
fun UndoButton(vm: GameBoardViewModel) {
    val canUndo by vm.canUndo.collectAsState()
    val winner by vm.winner.collectAsState()

    if (winner == null) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { vm.undo() },
                enabled = canUndo,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canUndo) MaterialTheme.colorScheme.primary else Color.LightGray,
                    contentColor = if (canUndo) MaterialTheme.colorScheme.onPrimary else Color.Gray
                )
            ) {
                Text("Undo")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRoundOneBottomBar() {
    RoundOneBottomBar(
        rememberSinglePlayerGameBoardForPreview(),
        onClickHome = { /* Navigate to home page*/ }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewRoundTwoBottomBar() {
    RoundTwoBottomBar(
        rememberSinglePlayerGameBoardForPreview(),
        onClickHome = { /* Navigate to home page*/ },
        onClickGameConfig = { /* Navigate to game configuration page*/ }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewUndoButton() {
    UndoButton(
        rememberSinglePlayerGameBoardForPreview()
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewWinningRoundDialog() {
    WinningRoundDialog(
        Role.WHITE,
        rememberSinglePlayerGameBoardForPreview()
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewGameOverDialog() {
    GameOverDialog(
        rememberSinglePlayerGameBoardForPreview(),
        GameResult.Winner(Player.FirstBlackPlayer),
        onClickHome = { /* Navigate to home page*/ }
    )
}