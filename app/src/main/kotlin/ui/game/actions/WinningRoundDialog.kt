package org.keizar.android.ui.game.actions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.keizar.android.ui.game.GameBoardViewModel
import org.keizar.android.ui.game.rememberSinglePlayerGameBoardForPreview
import org.keizar.game.Role
import org.keizar.utils.communication.game.Player

@Composable
fun WinningRoundOneDialog(
    winner: Role?,
    vm: GameBoardViewModel,
    showFlag: MutableState<Boolean> = mutableStateOf(false)
) {
    val currentRoundCount by vm.currentRoundCount.collectAsState()
    val endRoundOneAnnounced by vm.endRoundOneAnnounced.collectAsState()

    when (winner) {
        null -> {}

        else -> {
            val stats = vm.latestRoundStats.collectAsState(initial = null).value

            val roundCountText = if (currentRoundCount == 0) "First Round" else "Second Round"
            val winningStatusText =
                if (winner == vm.selfRole.collectAsState().value) "You Win" else "You Lose"
            if (showFlag.value || !endRoundOneAnnounced) {
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
                        if (stats != null) {
                            if (currentRoundCount == 0) {
                                if (vm.selfPlayer == Player.FirstBlackPlayer) {
                                    Text(
                                        text = "You captured: ${stats.neutralStats.blackCaptured}\n" +
                                                "Opponent captured: ${stats.neutralStats.whiteCaptured}\n" +
                                                "Number of moves: ${stats.neutralStats.blackMoves}\n" +
                                                "Time: ${stats.neutralStats.blackTime / 60} m ${stats.neutralStats.blackTime % 60} s \n" +
                                                "Your moves' average time: ${
                                                    String.format(
                                                        "%.2f",
                                                        stats.neutralStats.blackAverageTime
                                                    )
                                                } s",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "You captured: ${stats.neutralStats.whiteCaptured}\n" +
                                                "Opponent captured: ${stats.neutralStats.blackCaptured}\n" +
                                                "Number of moves: ${stats.neutralStats.whiteMoves}\n" +
                                                "Time: ${stats.neutralStats.whiteTime / 60} m ${stats.neutralStats.whiteTime % 60} s \n" +
                                                "Your moves' average time: ${
                                                    String.format(
                                                        "%.2f",
                                                        stats.neutralStats.whiteAverageTime
                                                    )
                                                } s",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )

                                }
                            } else {
                                if (vm.selfPlayer == Player.FirstBlackPlayer) {
                                    Text(
                                        text = "You captured: ${stats.neutralStats.whiteCaptured}\n" +
                                                "Opponent captured: ${stats.neutralStats.blackCaptured}\n" +
                                                "Number of moves: ${stats.neutralStats.whiteMoves}\n" +
                                                "Time: ${stats.neutralStats.whiteTime / 60} m ${stats.neutralStats.whiteTime % 60} s \n" +
                                                "Your moves' average time: ${
                                                    String.format(
                                                        "%.2f",
                                                        stats.neutralStats.whiteAverageTime
                                                    )
                                                } s",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "You captured: ${stats.neutralStats.blackCaptured}\n" +
                                                "Opponent captured: ${stats.neutralStats.whiteCaptured}\n" +
                                                "Number of moves: ${stats.neutralStats.blackMoves}\n" +
                                                "Time: ${stats.neutralStats.blackTime / 60} m ${stats.neutralStats.blackTime % 60} s \n" +
                                                "Your moves' average time: ${
                                                    String.format(
                                                        "%.2f",
                                                        stats.neutralStats.blackAverageTime
                                                    )
                                                } s",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
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
fun WinningRoundTwoDialog(
    winner: Role?,
    vm: GameBoardViewModel,
    showFlag: MutableState<Boolean> = mutableStateOf(false)
) {
    val currentRoundCount by vm.currentRoundCount.collectAsState()
    val endRoundTwoAnnounced by vm.endRoundTwoAnnounced.collectAsState()

    when (winner) {
        null -> {}

        else -> {
            val stats = vm.latestRoundStats.collectAsState(initial = null).value

            val roundCountText = "Second Round"
            val winningStatusText =
                if (winner == vm.selfRole.collectAsState().value) "You Win" else "You Lose"
            if (showFlag.value || !endRoundTwoAnnounced) {
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
                        if (stats != null) {
                            if (currentRoundCount == 0) {
                                if (vm.selfPlayer == Player.FirstBlackPlayer) {
                                    Text(
                                        text = "You captured: ${stats.neutralStats.blackCaptured}\n" +
                                                "Opponent captured: ${stats.neutralStats.whiteCaptured}\n" +
                                                "Number of moves: ${stats.neutralStats.blackMoves}\n" +
                                                "Time: ${stats.neutralStats.blackTime / 60} m ${stats.neutralStats.blackTime % 60} s \n" +
                                                "Your moves' average time: ${
                                                    String.format(
                                                        "%.2f",
                                                        stats.neutralStats.blackAverageTime
                                                    )
                                                } s",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "You captured: ${stats.neutralStats.whiteCaptured}\n" +
                                                "Opponent captured: ${stats.neutralStats.blackCaptured}\n" +
                                                "Number of moves: ${stats.neutralStats.whiteMoves}\n" +
                                                "Time: ${stats.neutralStats.whiteTime / 60} m ${stats.neutralStats.whiteTime % 60} s \n" +
                                                "Your moves' average time: ${
                                                    String.format(
                                                        "%.2f",
                                                        stats.neutralStats.whiteAverageTime
                                                    )
                                                } s",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )

                                }
                            } else {
                                if (vm.selfPlayer == Player.FirstBlackPlayer) {
                                    Text(
                                        text = "You captured: ${stats.neutralStats.whiteCaptured}\n" +
                                                "Opponent captured: ${stats.neutralStats.blackCaptured}\n" +
                                                "Number of moves: ${stats.neutralStats.whiteMoves}\n" +
                                                "Time: ${stats.neutralStats.whiteTime / 60} m ${stats.neutralStats.whiteTime % 60} s \n" +
                                                "Your moves' average time: ${
                                                    String.format(
                                                        "%.2f",
                                                        stats.neutralStats.whiteAverageTime
                                                    )
                                                } s",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    Text(
                                        text = "You captured: ${stats.neutralStats.blackCaptured}\n" +
                                                "Opponent captured: ${stats.neutralStats.whiteCaptured}\n" +
                                                "Number of moves: ${stats.neutralStats.blackMoves}\n" +
                                                "Time: ${stats.neutralStats.blackTime / 60} m ${stats.neutralStats.blackTime % 60} s \n" +
                                                "Your moves' average time: ${
                                                    String.format(
                                                        "%.2f",
                                                        stats.neutralStats.blackAverageTime
                                                    )
                                                } s",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
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

@Preview(showBackground = true)
@Composable
private fun PreviewWinningRoundDialog() {
    WinningRoundOneDialog(
        Role.WHITE,
        rememberSinglePlayerGameBoardForPreview()
    )
}
