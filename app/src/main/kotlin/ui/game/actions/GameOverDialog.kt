package org.keizar.android.ui.game.actions

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.keizar.android.ui.game.GameBoardViewModel
import org.keizar.android.ui.game.MultiplayerGameBoardViewModel
import org.keizar.android.ui.game.rememberSinglePlayerGameBoardForPreview
import org.keizar.utils.communication.game.GameResult
import org.keizar.utils.communication.game.Player

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
                val round1stats = vm.round1Statistics.collectAsState(initial = null).value
                val round2stats = vm.round2Statistics.collectAsState(initial = null).value
                var statText = ""
                if (vm is MultiplayerGameBoardViewModel) {
                    val myName by vm.myUser.collectAsState(initial = "")
                    val opponentName by vm.opponentUser.collectAsState(initial = "")
                    if (round1stats != null && round2stats != null) {
                        if (vm.selfPlayer == Player.FirstBlackPlayer) {
                            statText =
                                "$myName captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                        "$opponentName captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                        "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                        "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                        "Time: ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime) / 60} m ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime) % 60} s \n" +
                                        "Your moves' average time in first round: ${
                                            String.format(
                                                "%.4f",
                                                round1stats.neutralStats.blackAverageTime
                                            )
                                        } s\n" +
                                        "Your moves' average time in second round: ${
                                            String.format(
                                                "%.4f",
                                                round2stats.neutralStats.whiteAverageTime
                                            )
                                        } s\n"
                        } else {
                            statText =
                                "$myName captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                        "$opponentName captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                        "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                        "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                        "Time: ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) / 60} m ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) % 60} s \n" +
                                        "Your moves' average time in first round: ${
                                            String.format(
                                                "%.4f",
                                                round1stats.neutralStats.whiteAverageTime
                                            )
                                        } s\n" +
                                        "Your moves' average time in second round: ${
                                            String.format(
                                                "%.4f",
                                                round2stats.neutralStats.blackAverageTime
                                            )
                                        } s\n"
                        }
                    }
                } else {
                    if (round1stats != null && round2stats != null) {
                        if (vm.selfPlayer == Player.FirstBlackPlayer) {
                            statText =
                                "You captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                        "Opponent captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                        "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                        "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                        "Time: ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime / 60)} m ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime) % 60} s \n" +
                                        "Your moves' average time in first round: ${
                                            String.format(
                                                "%.4f",
                                                round1stats.neutralStats.blackAverageTime
                                            )
                                        } s\n" +
                                        "Your moves' average time in second round: ${
                                            String.format(
                                                "%.4f",
                                                round2stats.neutralStats.whiteAverageTime
                                            )
                                        } \ns"
                        } else {
                            statText =
                                "You captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                        "Opponent captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                        "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                        "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                        "Time: ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) / 60} m ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) % 60} s \n" +
                                        "Your moves' average time in first round: ${
                                            String.format(
                                                "%.4f",
                                                round1stats.neutralStats.whiteAverageTime
                                            )
                                        } s\n" +
                                        "Your moves' average time in second round: ${
                                            String.format(
                                                "%.4f",
                                                round2stats.neutralStats.blackAverageTime
                                            )
                                        } s\n"
                        }
                    }
                }
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
                            text = statText,
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
                    }
                )
            }


            is GameResult.Winner -> {
                val round1stats = vm.round1Statistics.collectAsState(initial = null).value
                val round2stats = vm.round2Statistics.collectAsState(initial = null).value
                var statText = ""
                val winnerText = if (finalWinner.player == vm.selfPlayer) "You Win" else "You Lose"
                if (vm is MultiplayerGameBoardViewModel) {
                    val myName by vm.myUser.collectAsState(initial = "")
                    val opponentName by vm.opponentUser.collectAsState(initial = "")
                    if (round1stats != null && round2stats != null) {
                        if (vm.selfPlayer == Player.FirstBlackPlayer) {
                            statText =
                                "$myName captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                        "$opponentName captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                        "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                        "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                        "Time: ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime) / 60} m ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime) % 60} s \n" +
                                        "Your moves' average time in first round: ${
                                            String.format(
                                                "%.4f",
                                                round1stats.neutralStats.blackAverageTime
                                            )
                                        } s\n" +
                                        "Your moves' average time in second round: ${
                                            String.format(
                                                "%.4f",
                                                round2stats.neutralStats.whiteAverageTime
                                            )
                                        } s\n"
                        } else {
                            statText =
                                "$myName captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                        "$opponentName captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                        "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                        "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                        "Time: ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) / 60} m ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) % 60} s \n" +
                                        "Your moves' average time in first round: ${
                                            String.format(
                                                "%.4f",
                                                round1stats.neutralStats.whiteAverageTime
                                            )
                                        } s\n" +
                                        "Your moves' average time in second round: ${
                                            String.format(
                                                "%.4f",
                                                round2stats.neutralStats.blackAverageTime
                                            )
                                        } s\n"
                        }
                    }
                } else {
                    if (round1stats != null && round2stats != null) {
                        if (vm.selfPlayer == Player.FirstBlackPlayer) {
                            statText =
                                "You captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                        "Opponent captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                        "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                        "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                        "Time: ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime) / 60} m ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime) % 60} s \n" +
                                        "Your moves' average time in first round: ${
                                            String.format(
                                                "%.4f",
                                                round1stats.neutralStats.blackAverageTime
                                            )
                                        } s\n" +
                                        "Your moves' average time in second round: ${
                                            String.format(
                                                "%.4f",
                                                round2stats.neutralStats.whiteAverageTime
                                            )
                                        } s\n"
                        } else {
                            statText =
                                "You captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                        "Opponent captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                        "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                        "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                        "Time: ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) / 60} m ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) % 60} s \n" +
                                        "Your moves' average time in first round: ${
                                            String.format(
                                                "%.4f",
                                                round1stats.neutralStats.whiteAverageTime
                                            )
                                        } s\n" +
                                        "Your moves' average time in second round: ${
                                            String.format(
                                                "%.4f",
                                                round2stats.neutralStats.blackAverageTime
                                            )
                                        } s\n"
                        }
                    }
                }

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
                            text = statText,
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
                    }
                )

            }
        }
    }
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