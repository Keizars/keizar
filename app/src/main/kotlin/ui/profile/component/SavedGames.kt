package org.keizar.android.ui.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.keizar.android.data.GameStartConfigurationEncoder
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.game.BoardTiles
import org.keizar.android.ui.profile.ProfileViewModel
import org.keizar.game.BoardProperties
import org.keizar.utils.communication.game.GameDataGet
import org.keizar.utils.communication.game.Player
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun SavedGames(modifier: Modifier = Modifier, vm: ProfileViewModel) {
    if (vm.isLoadingGames.collectAsStateWithLifecycle().value) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text(text = "Loading Games")
        }
    }
    val allGames = vm.allGames.collectAsStateWithLifecycle(emptyList())
    val selectedGame by vm.selectedGame.collectAsStateWithLifecycle()
    if (isSystemInLandscape()) {
        Row {
            LazyColumn(
                modifier = modifier
                    .padding(4.dp)
                    .wrapContentSize()
            ) {
                items(allGames.value) { gameData ->
                    SavedGameCard(
                        vm = vm,
                        gameData = gameData,
                        modifier = Modifier
                            .padding(4.dp)
                            .defaultMinSize(200.dp),
                        onclick = { vm.selectedGame.value = gameData }
                    )
                }
            }

            if (allGames.value.isNotEmpty()) {
                GameDetailColumn(
                    modifier = modifier,
                    gameData = selectedGame
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(4.dp)
                .wrapContentSize()
        ) {
            items(allGames.value) { gameData ->
                SavedGameCard(
                    vm = vm,
                    gameData = gameData,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                )
            }
        }
    }

}

@Composable
fun SavedGameCard(
    modifier: Modifier = Modifier,
    onclick: () -> Unit = {},
    vm: ProfileViewModel, gameData: GameDataGet
) {
    var showDetails by remember { mutableStateOf(false) }
    val round1stats = gameData.round1Stats
    val round2stats = gameData.round2Stats
    val savedGameCardViewModel = remember(gameData) {
        SavedGameViewModel(
            gameData = gameData,
            vm = vm
        )
    }
    val opponentName = gameData.opponentUsername
    Card(
        modifier = modifier,
        onClick = {
            showDetails = true
            onclick()
        }
    ) {
        val avatarUrl by savedGameCardViewModel.avatarUrl.collectAsStateWithLifecycle()
        val filePath by savedGameCardViewModel.filePath.collectAsStateWithLifecycle()

        Row(
            modifier = modifier
                .height(IntrinsicSize.Min)
        ) {
            AvatarImage(
                url = avatarUrl,
                modifier = Modifier
                    .size(72.dp)
                    .padding(8.dp),
                filePath = filePath
            )

            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {

                val winner: Player? = if (round1stats.winner!! == round2stats.winner!!) {
                    round1stats.winner!!
                } else {
                    if (round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured >
                        round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured
                    ) {
                        Player.FirstBlackPlayer
                    } else if (round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured <
                        round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured
                    ) {
                        Player.FirstWhitePlayer
                    } else {
                        null
                    }
                }

                val winningStatus = if (winner == null) {
                    "Draw"
                } else {
                    if (winner == round1stats.player) {
                        "Win"
                    } else {
                        "Loss"
                    }
                }

                val localDateTime =
                    remember { LocalDateTime.ofInstant(Instant.parse(gameData.timeStamp), ZoneId.systemDefault()) }
                val formatter = remember {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                }
                val formattedTimeStamp = remember {
                    formatter.format(localDateTime)
                }
                Text(
                    text = "$winningStatus - $formattedTimeStamp",
                    modifier = Modifier.padding(4.dp)
                )

                Text(text = "Opponent: $opponentName", modifier = Modifier.padding(4.dp))


                if (showDetails && !isSystemInLandscape()) {
                    GameDetailsDialog(
                        gameData = gameData, onDismissRequest = { showDetails = false },
                    )
                }
            }
            Column(modifier = modifier, horizontalAlignment = Alignment.End) {
                var showMenu by remember { mutableStateOf(false) }
                Box(
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = { showMenu = !showMenu }
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {

                        DropdownMenuItem(onClick = {
                            showMenu = false
                            vm.launchInBackground {
                                vm.deleteGame(gameData.dataId)
                            }
                        }) {
                            Text("Delete")
                        }
                    }
                }
            }

        }
    }
}


@Composable
fun GameDetailColumn(
    modifier: Modifier = Modifier,
    gameData: GameDataGet? = null
) {
    Column {
        Detail(modifier, gameData, showOK = false)
    }
}

@Composable
fun GameDetailsDialog(
    modifier: Modifier = Modifier,
    gameData: GameDataGet,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Detail(modifier, gameData, onDismissRequest, true)
    }
}

@Composable
fun Detail(
    modifier: Modifier,
    gameData: GameDataGet?,
    onDismissRequest: () -> Unit = {},
    showOK: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
            .alpha(if (gameData == null) 0.0f else 1f)
            .alpha(if (gameData == null) 0.0f else 1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val layoutSeed =
                gameData?.let { GameStartConfigurationEncoder.decode(it.gameConfiguration)?.layoutSeed }
            val boardProperties = BoardProperties.getStandardProperties(layoutSeed)
            Box(
                Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                BoardTiles(
                    rotationDegrees = 0f,
                    properties = boardProperties,
                    currentPick = null,
                    onClickTile = {},
                    Modifier.matchParentSize(),
                )
            }
        }
        if (gameData != null) {
            val round1stats = gameData.round1Stats
            val round2stats = gameData.round2Stats
            val myName = gameData.selfUsername
            val opponentName = gameData.opponentUsername

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Game Statistics",
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                    val statText: String
                    if (round1stats.player == Player.FirstBlackPlayer) {
                        statText =
                            "$myName captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                    "$opponentName captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                    "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                    "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                    "Time Taken: ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime) / 60} m ${(round1stats.neutralStats.blackTime + round2stats.neutralStats.whiteTime) % 60} s \n" +
                                    "Your moves' average time in round 1: ${
                                        String.format(
                                            "%.2f",
                                            round1stats.neutralStats.blackAverageTime
                                        )
                                    } s\n" +
                                    "Your moves' average time in round 2: ${
                                        String.format(
                                            "%.2f",
                                            round2stats.neutralStats.whiteAverageTime
                                        )
                                    } s\n"
                    } else {
                        statText =
                            "$myName captured: ${round1stats.neutralStats.whiteCaptured + round2stats.neutralStats.blackCaptured}\n" +
                                    "$opponentName captured: ${round1stats.neutralStats.blackCaptured + round2stats.neutralStats.whiteCaptured}\n" +
                                    "Number of moves in round 1: ${round1stats.neutralStats.blackMoves}\n" +
                                    "Number of moves in round 2: ${round2stats.neutralStats.blackMoves}\n" +
                                    "Time Taken: ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) / 60} m ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) % 60} s \n" +
                                    "Your moves' average time in round 1: ${
                                        String.format(
                                            "%.2f",
                                            round1stats.neutralStats.whiteAverageTime
                                        )
                                    } s\n" +
                                    "Your moves' average time in round 2: ${
                                        String.format(
                                            "%.2f",
                                            round2stats.neutralStats.blackAverageTime
                                        )
                                    } s\n"
                    }

                    Text(
                        text = statText,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                    if (showOK) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Button(onClick = onDismissRequest) {
                                Text(text = "OK")
                            }
                        }
                    }
                }
            }
        }
    }
}