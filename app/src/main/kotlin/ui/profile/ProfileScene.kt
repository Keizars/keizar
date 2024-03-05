package org.keizar.android.ui.profile

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.keizar.android.GameStartConfigurationEncoder
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.foundation.pagerTabIndicatorOffset
import org.keizar.android.ui.game.BoardTiles
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.game.BoardProperties
import org.keizar.game.Difficulty
import org.keizar.game.Role
import org.keizar.utils.communication.game.GameDataGet
import org.keizar.utils.communication.game.NeutralStats
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.game.RoundStats
import java.io.File

@Composable
fun ProfileScene(
    vm: ProfileViewModel,
    onClickBack: () -> Unit,
    onClickPlayGame: (String) -> Unit,
    onClickPasswordEdit: () -> Unit,
    onSuccessfulEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Account")
                },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showDropdown by remember {
                        mutableStateOf(false)
                    }
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }) {
                        DropdownMenuItem(
                            onClick = {
                                showDropdown = false
                                vm.launchInBackground {
                                    logout()
                                    withContext(Dispatchers.Main) {
                                        onClickBack()
                                    }
                                }
                            },
                            text = { Text(text = "Log out") },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Log out",
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                showDropdown = false
                                onClickPasswordEdit()
                            },
                            text = { Text(text = "Change Password") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = "Password",
                                )
                            }
                        )
                    }
                    IconButton(onClick = { showDropdown = !showDropdown }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                        )
                    }
                }
            )
        },
    ) { contentPadding ->
        Column(
            Modifier
                .padding(contentPadding)
        ) {
            if (vm.refresh.value) {
                ProfilePage(
                    vm = vm,
                    onSuccessfulEdit = onSuccessfulEdit,
                    Modifier.fillMaxSize(),
                    onClickPlayGame = onClickPlayGame
                )
            } else {
                ProfilePage(
                    vm = vm,
                    onSuccessfulEdit = onSuccessfulEdit,
                    Modifier.fillMaxSize(),
                    onClickPlayGame = onClickPlayGame
                )
            }
        }
    }
}

@Composable
fun ProfilePage(
    vm: ProfileViewModel,
    onSuccessfulEdit: () -> Unit,
    modifier: Modifier = Modifier,
    onClickPlayGame: (String) -> Unit
) {
    val self by vm.self.collectAsStateWithLifecycle(null)
    Column(
        modifier = modifier
    ) {
        Row(
            Modifier
                .background(MaterialTheme.colorScheme.surface)
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
                .fillMaxWidth()
                .height(64.dp),
        ) {
            val context = LocalContext.current
            val imagePicker = rememberImagePicker(onImageSelected = { files ->
                vm.launchInBackground {
                    context.contentResolver.openInputStream(files.first())?.use {
                        vm.uploadAvatar(it)
                    } ?: throw IllegalArgumentException("Failed to open input stream for $files")
                }
            })

            Box(
                Modifier
                    .clip(CircleShape)
                    .clickable {
                        imagePicker.launchPhotoPicker()
                    }
            ) {
                AvatarImage(
                    url = self?.avatarUrlOrDefault(),
                    Modifier.size(64.dp),
                )
            }

            Column(
                Modifier
                    .padding(start = 16.dp)
                    .fillMaxHeight()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = vm.nickname.collectAsStateWithLifecycle().value,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(20.dp)
                            .clickable(onClick = { vm.showDialog() })
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

//                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = self?.username ?: "Loading...",
                    Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

//        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        val pagerState = rememberPagerState(initialPage = 0) { 3 }
        val scope = rememberCoroutineScope()

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = @Composable { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = {
                    Text(text = "Saved Boards")
                }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } }, text = {
                    Text(text = "Saved Games")
                }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                text = {
                    Text(text = "Statistics")
                }
            )
        }

        if (vm.showNicknameEditDialog.value) {
            NicknameEditDialog(vm = vm, onSuccessfulEdit = onSuccessfulEdit)
        }
        HorizontalPager(state = pagerState, Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                when (it) {
                    0 -> SavedBoards(vm = vm, Modifier.fillMaxSize(), onClickPlayGame = onClickPlayGame)
                    1 -> SavedGames(vm = vm)
                    2 -> Statistics()
                }
            }
        }
    }
}

@Composable
fun NicknameEditDialog(
    modifier: Modifier = Modifier,
    vm: ProfileViewModel,
    onSuccessfulEdit: () -> Unit
) {
    val nicknameError by vm.nicknameError.collectAsStateWithLifecycle()
    AlertDialog(onDismissRequest = {},
        title = {
            Text(
                text = "Edit Nickname",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            OutlinedTextField(
                value = vm.editNickname.value,
                onValueChange = { vm.setEditNickname(it) },
                modifier = Modifier.fillMaxWidth(),
                isError = (nicknameError != null),
                label = { Text("New nickname") },
                shape = RoundedCornerShape(8.dp),
            )
        },
        confirmButton = {
            Button(onClick = {
                vm.launchInBackground {
                    vm.confirmDialog()
                    withContext(Dispatchers.Main) {
                        onSuccessfulEdit()
                    }
                }
            }) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                vm.cancelDialog()
            }) {
                Text(text = "Cancel")
            }
        })
}

@Composable
fun AvatarImage(url: String?, modifier: Modifier = Modifier, filePath: String? = null) {
    AsyncImage(
        model = if (filePath != null) ImageRequest.Builder(LocalContext.current)
            .data(File(filePath))
            .crossfade(true)
            .build() else url,
        contentDescription = "Avatar",
        modifier,
        placeholder = rememberVectorPainter(Icons.Default.Person),
        error = rememberVectorPainter(Icons.Default.Person),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun SavedBoards(vm: ProfileViewModel, modifier: Modifier = Modifier, onClickPlayGame: (String) -> Unit) {
    val allSeeds by vm.allSeeds.collectAsState()
    if (!isSystemInLandscape()) {
        SavedBoardCardsSummary(modifier = Modifier.fillMaxWidth(), vm = vm, allSeeds = allSeeds, onClickPlayGame = onClickPlayGame)
    } else {
        Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
            val selectedSeed by vm.selectedSeed.collectAsStateWithLifecycle()
            SavedBoardCardsSummary(modifier = Modifier.wrapContentSize(), vm = vm, allSeeds = allSeeds, onClickPlayGame = onClickPlayGame)

            VerticalDivider(Modifier.padding(horizontal = 8.dp))

            Column(
                modifier = Modifier
                    .fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val boardProperties = remember(selectedSeed) {
                    selectedSeed?.boardProperties ?: BoardProperties.getStandardProperties()
                }
                Box(
                    Modifier
                        .size(500.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .alpha(if (selectedSeed == null) 0.0f else 1f)
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
        }
    }
}

@Composable
private fun SavedBoardCardsSummary(modifier: Modifier = Modifier, vm: ProfileViewModel, allSeeds: List<SavedSeed>, onClickPlayGame: (String) -> Unit){
    LazyColumn(
        modifier = Modifier
            .wrapContentSize()
            .padding(4.dp)
    ) {
        items(allSeeds) { seed ->
            SavedBoardCard(
                savedSeed = seed,
                vm = vm,
                modifier = modifier
                    .padding(4.dp)
                    .defaultMinSize(200.dp),
                onClickPlayGame = onClickPlayGame
            )
        }
    }
}

@Composable
fun SavedBoardCard(
    modifier: Modifier,
    savedSeed: SavedSeed,
    vm: ProfileViewModel,
    onClickPlayGame: (String) -> Unit
) {
    Card({ vm.selectedSeed.value = savedSeed }, modifier = modifier.wrapContentSize()) {
        Row(
            Modifier
                .height(IntrinsicSize.Min)
                .padding(4.dp)
        ) {
            Box(
                Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                BoardTiles(
                    rotationDegrees = 0f,
                    properties = savedSeed.boardProperties,
                    currentPick = null,
                    onClickTile = {},
                    Modifier.matchParentSize(),
                )
            }
            Column(modifier = Modifier, horizontalAlignment = Alignment.End) {
                Box(
                    Modifier,
                    contentAlignment = Alignment.TopEnd
                ) {
                    var showMenu by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {

                        DropdownMenuItem(onClick = {
                            showMenu = false
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Copied seed", savedSeed.configurationSeed)
                            clipboard.setPrimaryClip(clip)
                        }) {
                            Text("Copy seed")
                        }
                        DropdownMenuItem(onClick = {
                            showMenu = false
                            vm.launchInBackground {
                                vm.removeSeed(savedSeed.configurationSeed)
                                vm.selectedSeed.value = null
                            }
                        }) {
                            Text("Delete")
                        }
                    }
                }
                Column(
                    modifier
                        .padding(4.dp)
                        .fillMaxHeight(),
                ) {

                    Text(
                        text = "Game board seed: \n${savedSeed.configurationSeed}",
                        Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    Button(modifier = Modifier.align(Alignment.End), onClick = { onClickPlayGame(savedSeed.configurationSeed) }) {
                        Text(text = "Play")
                    }
                }
            }
        }
    }
}

@Composable
fun SavedGames(modifier: Modifier = Modifier, vm: ProfileViewModel) {
    val allGames = vm.allGames.collectAsStateWithLifecycle(emptyList())
    if (isSystemInLandscape()) {
        Row {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = modifier.padding(4.dp)
            ) {
                items(allGames.value) { gameData ->
                    SavedGameCard(
                        vm = vm,
                        gameData = gameData,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
            if (allGames.value.isNotEmpty()) {
                GameDetailColumn(
                    modifier = modifier,
                    gameData = allGames.value.first(),
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.padding(4.dp)
        ) {
            items(allGames.value) { gameData ->
                SavedGameCard(
                    vm = vm,
                    gameData = gameData,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }

}

@Composable
fun SavedGameCard(
    modifier: Modifier = Modifier,
    vm: ProfileViewModel, gameData: GameDataGet
) {
    var showDetails by remember { mutableStateOf(false) }
    val round1stats = gameData.round1Stats
    val round2stats = gameData.round2Stats

    val opponentName = gameData.opponentUsername
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { showDetails = true }
    ) {
        var avatarUrl = ""
        var filePath: String? = null
        if (opponentName == "Computer") {
            filePath = "app/src/main/res/drawable/robot_icon.png"
        } else {
            vm.launchInBackground {
                avatarUrl = vm.getAvatarUrl(opponentName)
            }
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
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

                val winningStatus = if (round1stats.winner!! == round2stats.winner!!) {
                    if (round1stats.winner == round1stats.player) {
                        "Win"
                    } else {
                        "Lose"
                    }
                } else {
                    "Draw"
                }

                Text(
                    text = "$winningStatus - ${gameData.timeStamp}",
                    modifier = Modifier.padding(4.dp)
                )
                Text(text = "Opponent: $opponentName", modifier = Modifier.padding(4.dp))
            }

            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.End) {
                var showMenu by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showMenu = !showMenu }, modifier = Modifier
                        .size(42.dp)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More options")
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
                if (showDetails) {
                    GameDetailsDialog(
                        gameData = gameData, onDismissRequest = { showDetails = false },
                    )
                }
            }
        }
    }

}

@Composable
private fun GameDetailColumn(
    modifier: Modifier = Modifier,
    gameData: GameDataGet,
) {
    Column {
        Detail(modifier, gameData, showOK = false)
    }
}

@Composable
private fun GameDetailsDialog(
    modifier: Modifier = Modifier,
    gameData: GameDataGet,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Detail(modifier, gameData, onDismissRequest, true)
    }
}

@Composable
private fun Detail(
    modifier: Modifier,
    gameData: GameDataGet,
    onDismissRequest: () -> Unit = {},
    showOK: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val layoutSeed =
                GameStartConfigurationEncoder.decode(gameData.gameConfiguration)?.layoutSeed
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
                    .background(MaterialTheme.colorScheme.secondaryContainer)
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
                                        "%.4f",
                                        round1stats.neutralStats.blackAverageTime
                                    )
                                } s\n" +
                                "Your moves' average time in round 2: ${
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
                                "Time Taken: ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) / 60} m ${(round1stats.neutralStats.whiteTime + round2stats.neutralStats.blackTime) % 60} s \n" +
                                "Your moves' average time in round 1: ${
                                    String.format(
                                        "%.4f",
                                        round1stats.neutralStats.whiteAverageTime
                                    )
                                } s\n" +
                                "Your moves' average time in round 2: ${
                                    String.format(
                                        "%.4f",
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

@Composable
private fun Statistics(modifier: Modifier = Modifier) {
    // TODO: Statistics
}

@Preview(showBackground = true)
@Preview(showBackground = true, fontScale = 2f)
@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun PreviewProfilePage() {
    ProvideCompositionalLocalsForPreview {
        ProfileScene(
            remember {
                ProfileViewModel().apply {
                    allSeeds.value = listOf(
                        SavedSeed("123"),
                        SavedSeed("456"),
                        SavedSeed("789")
                    )
                }
            },
            onClickBack = {},
            onClickPasswordEdit = {},
            onSuccessfulEdit = {},
            onClickPlayGame = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSavedBoardCardPhone() {
    val vm = ProfileViewModel()
    SavedBoardCard(
        savedSeed = SavedSeed(
            GameStartConfigurationEncoder.encode(
                GameStartConfiguration(
                    layoutSeed = 123,
                    playAs = Role.WHITE,
                    difficulty = Difficulty.MEDIUM
                )
            )
        ), vm = vm, modifier = Modifier.fillMaxWidth(), onClickPlayGame = {}
    )
}

@Preview(showBackground = true, widthDp = 1000, device = Devices.TABLET)
@Composable
private fun PreviewSavedBoardCardTablet() {
    val vm = ProfileViewModel()
    SavedBoardCard(
        savedSeed = SavedSeed(
            GameStartConfigurationEncoder.encode(
                GameStartConfiguration(
                    layoutSeed = 123,
                    playAs = Role.WHITE,
                    difficulty = Difficulty.MEDIUM
                )
            )
        ), vm = vm, modifier = Modifier, onClickPlayGame = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewDialog() {
    val vm = ProfileViewModel()
    NicknameEditDialog(vm = vm, onSuccessfulEdit = {})
}

@Preview(showBackground = true)
@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun PreviewDialogInTablet() {
    val vm = ProfileViewModel()
    NicknameEditDialog(vm = vm, onSuccessfulEdit = {})
}

@Preview(showBackground = true)
@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun PreviewSavedGameCard() {
    val vm = ProfileViewModel()
    val round1Stats =
        RoundStats(
            NeutralStats(0, 0, 0.0, 0, 0, 0.0, 0, 0),
            Player.FirstBlackPlayer,
            Player.FirstWhitePlayer
        )
    val gameData = GameDataGet(
        "harrison", "harry", "2023-02-19", GameStartConfigurationEncoder.encode(
            GameStartConfiguration(
                layoutSeed = 123,
                playAs = Role.WHITE,
                difficulty = Difficulty.MEDIUM
            )
        ),
        round1Stats, round1Stats, "1"
    )
    SavedGameCard(vm = vm, gameData = gameData)
}

@Preview(showBackground = true, device = Devices.TABLET)
@Composable
private fun PreviewGameDetails() {
    val round1Stats =
        RoundStats(
            NeutralStats(0, 0, 0.0, 0, 0, 0.0, 0, 0),
            Player.FirstBlackPlayer,
            Player.FirstWhitePlayer
        )
    val gameData = GameDataGet(
        "harrison", "harry", "2023-02-19", GameStartConfigurationEncoder.encode(
            GameStartConfiguration(
                layoutSeed = 123,
                playAs = Role.WHITE,
                difficulty = Difficulty.MEDIUM
            )
        ),
        round1Stats, round1Stats, "1"
    )
    GameDetailsDialog(
        gameData = gameData,
        onDismissRequest = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewSavedGames() {
    val round1Stats =
        RoundStats(
            NeutralStats(0, 0, 0.0, 0, 0, 0.0, 0, 0),
            Player.FirstBlackPlayer,
            Player.FirstWhitePlayer
        )
    val gameData = GameDataGet(
        "harrison", "harry", "2023-02-19", GameStartConfigurationEncoder.encode(
            GameStartConfiguration(
                layoutSeed = 123,
                playAs = Role.WHITE,
                difficulty = Difficulty.MEDIUM
            )
        ),
        round1Stats, round1Stats, "1"
    )
    val vm = ProfileViewModel()
    GameDetailColumn(gameData = gameData, modifier = Modifier)
}

