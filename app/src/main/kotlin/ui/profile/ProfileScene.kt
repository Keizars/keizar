package org.keizar.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.keizar.android.GameStartConfigurationEncoder
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.foundation.pagerTabIndicatorOffset
import org.keizar.android.ui.game.BoardTiles
import org.keizar.android.ui.game.configuration.GameStartConfiguration
import org.keizar.game.BoardProperties
import org.keizar.game.Difficulty
import org.keizar.game.Role

@Composable
fun ProfileScene(
    vm: ProfileViewModel,
    onClickBack: () -> Unit,
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
                                    Icons.AutoMirrored.Filled.Login,
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
                    Modifier.fillMaxSize()
                )
            } else {
                ProfilePage(
                    vm = vm,
                    onSuccessfulEdit = onSuccessfulEdit,
                    Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ProfilePage(
    vm: ProfileViewModel,
    onSuccessfulEdit: () -> Unit,
    modifier: Modifier = Modifier
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
        HorizontalPager(state = pagerState) {
            Column(Modifier.fillMaxSize()) {
                when (it) {
                    0 -> SavedBoards(vm = vm)
                    1 -> SavedGames()
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
fun AvatarImage(url: String?, modifier: Modifier = Modifier) {
    AsyncImage(
        model = url,
        contentDescription = "Avatar",
        modifier,
        placeholder = rememberVectorPainter(Icons.Default.Person),
        error = rememberVectorPainter(Icons.Default.Person),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun SavedBoards(modifier: Modifier = Modifier, vm: ProfileViewModel) {
    val allSeeds by vm.allSeeds.collectAsState()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(4.dp)
    ) {
        items(allSeeds) { seed ->
            SavedBoardCard(
                layoutSeedText = seed,
                vm = vm,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun SavedBoardCard(modifier: Modifier = Modifier, layoutSeedText: String, vm: ProfileViewModel) {
    Card(modifier = modifier.fillMaxWidth()) {
        val layoutSeed = GameStartConfigurationEncoder.decode(layoutSeedText)?.layoutSeed
        Row(
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(4.dp)
        ) {
            val boardProperties = BoardProperties.getStandardProperties(layoutSeed)
            Box(
                Modifier
                    .size(150.dp)
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
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(onClick = {
                            showMenu = false
                            vm.launchInBackground { vm.removeSeed(layoutSeedText) }
                        }) {
                            Text("Delete")
                        }
                    }
                }
                Column(
                    Modifier
                        .padding(4.dp)
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Text(
                        text = "Game board seed: $layoutSeedText",
                        Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )

                }
            }
        }
    }
}

@Composable
fun SavedGames(modifier: Modifier = Modifier) {
    // TODO: SavedGames
}

@Composable
fun SavedGameCard(modifier: Modifier = Modifier, vm: ProfileViewModel) {
    //TODO: SavedGameCard
}

@Composable
private fun Statistics(modifier: Modifier = Modifier) {
    // TODO: Statistics
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfilePage() {
    ProvideCompositionalLocalsForPreview {
        ProfileScene(
            ProfileViewModel(),
            onClickBack = {},
            onClickPasswordEdit = {},
            onSuccessfulEdit = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSavedBoardCard() {
    val vm = ProfileViewModel()
    SavedBoardCard(
        layoutSeedText = GameStartConfigurationEncoder.encode(
            GameStartConfiguration(
                layoutSeed = 123,
                playAs = Role.WHITE,
                difficulty = Difficulty.MEDIUM
            )
        ), vm = vm
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewDialog() {
    val vm = ProfileViewModel()
    NicknameEditDialog(vm = vm, onSuccessfulEdit = {})
}