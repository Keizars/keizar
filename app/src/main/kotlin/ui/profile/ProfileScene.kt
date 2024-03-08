package org.keizar.android.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.keizar.android.data.GameStartConfigurationEncoder
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.defaultFocus
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.foundation.onKey
import org.keizar.android.ui.foundation.pagerTabIndicatorOffset
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.android.ui.game.mp.room.ConnectingRoomDialog
import org.keizar.android.ui.profile.component.GameDetailColumn
import org.keizar.android.ui.profile.component.GameDetailsDialog
import org.keizar.android.ui.profile.component.SavedBoardCard
import org.keizar.android.ui.profile.component.SavedBoards
import org.keizar.android.ui.profile.component.SavedGameCard
import org.keizar.android.ui.profile.component.SavedGames
import org.keizar.android.ui.profile.component.UserInfoRow
import org.keizar.utils.communication.game.Difficulty
import org.keizar.game.Role
import org.keizar.utils.communication.game.GameDataGet
import org.keizar.utils.communication.game.NeutralStats
import org.keizar.utils.communication.game.Player
import org.keizar.utils.communication.game.RoundStats

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
            ProfilePage(
                vm = vm,
                onSuccessfulEdit = onSuccessfulEdit,
                Modifier.fillMaxSize(),
                onClickPlayGame = onClickPlayGame
            )
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
        val context = LocalContext.current

        var showUploading by remember { mutableStateOf(false) }
        if (showUploading) {
            ConnectingRoomDialog(
                text = { Text(text = "Updating...") }
            )
        }

        UserInfoRow(
            self,
            onPickAvatar = { file ->
                if (showUploading) return@UserInfoRow
                vm.launchInBackground {
                    showUploading = true
                    try {
                        context.contentResolver.openInputStream(file)?.use {
                            vm.uploadAvatar(it)
                        } ?: throw IllegalArgumentException("Failed to open input stream for $file")
                    } finally {
                        showUploading = false
                    }
                }
            },
            onClickEditNickname = { vm.showEditNicknameDialog() },
            Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
        )

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
//            Tab(
//                selected = pagerState.currentPage == 1,
//                onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
//                text = {
//                    Text(text = "Statistics")
//                }
//            )
        }

        if (vm.showNicknameEditDialog.value) {
            NicknameEditDialog(vm = vm, onSuccessfulEdit = onSuccessfulEdit)
        }
        HorizontalPager(state = pagerState, Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                when (it) {
                    0 -> SavedBoards(
                        vm = vm,
                        Modifier.fillMaxSize(),
                        onClickPlayGame = onClickPlayGame
                    )

                    1 -> SavedGames(vm = vm, onClickPlayGame = onClickPlayGame)
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
    AlertDialog(
        onDismissRequest = {
            vm.cancelDialog()
        },
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
                isError = (nicknameError != null),
                label = { Text("New nickname") },
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = {
                    vm.launchInBackground {
                        vm.confirmDialog()
                    }
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultFocus()
                    .onKey(key = Key.Enter) {
                        vm.launchInBackground {
                            vm.confirmDialog()
                        }
                    },
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
        },
        modifier = modifier
    )
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

                    allGames.value = listOf(
                        GameDataGet(
                            "harrison", "harry", "2023-03-05T21:15:11.188631Z", GameStartConfigurationEncoder.encode(
                                GameStartConfiguration(
                                    layoutSeed = 123,
                                    playAs = Role.WHITE,
                                    difficulty = Difficulty.MEDIUM
                                )
                            ),
                            RoundStats(
                                NeutralStats(0, 0, 0.0, 0, 0, 0.0, 0, 0),
                                Player.FirstBlackPlayer,
                                Player.FirstWhitePlayer
                            ),
                            RoundStats(
                                NeutralStats(0, 0, 0.0, 0, 0, 0.0, 0, 0),
                                Player.FirstBlackPlayer,
                                Player.FirstWhitePlayer
                            ),
                            "1"

                        ),
                        GameDataGet(
                            "harrison", "harry", "2023-03-05T21:15:11.188631Z", GameStartConfigurationEncoder.encode(
                                GameStartConfiguration(
                                    layoutSeed = 456,
                                    playAs = Role.WHITE,
                                    difficulty = Difficulty.MEDIUM
                                )
                            ),
                            RoundStats(
                                NeutralStats(0, 0, 0.0, 0, 0, 0.0, 0, 0),
                                Player.FirstBlackPlayer,
                                Player.FirstWhitePlayer
                            ),
                            RoundStats(
                                NeutralStats(0, 0, 0.0, 0, 0, 0.0, 0, 0),
                                Player.FirstBlackPlayer,
                                Player.FirstWhitePlayer
                            ),
                            "1"

                        )
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
    GameDetailColumn(gameData = gameData, modifier = Modifier)
}

