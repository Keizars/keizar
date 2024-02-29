package org.keizar.android.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.keizar.android.ui.foundation.ProvideCompositionalLocalsForPreview
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.foundation.pagerTabIndicatorOffset

@Composable
fun ProfileScene(
    vm: ProfileViewModel,
    onClickBack: () -> Unit,
    onClickEdit: () -> Unit,
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
                    DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
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
                onClickEdit = onClickEdit,
                Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ProfilePage(
    vm: ProfileViewModel,
    onClickEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val self by vm.self.collectAsStateWithLifecycle(null)
    Column(
        modifier = modifier
    ) {
        Row(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
                .fillMaxWidth()
                .height(64.dp),
        ) {
            Box(
                Modifier
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = self?.avatarUrl,
                    contentDescription = "",
                    Modifier.size(64.dp),
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    error = rememberVectorPainter(Icons.Default.Person)
                )
            }

            Column(
                Modifier
                    .padding(start = 16.dp)
                    .fillMaxHeight()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = self?.nickname ?: "Loading...", style = MaterialTheme.typography.titleMedium)

                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(20.dp)
                            .clickable(onClick = onClickEdit)
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

        HorizontalPager(state = pagerState) {
            Column(Modifier.fillMaxSize()) {
                when (it) {
                    0 -> SavedBoards()
                    1 -> SavedGames()
                    2 -> Statistics()
                }
            }
        }
    }
}

@Composable
fun SavedBoards(modifier: Modifier = Modifier) {
    // TODO: SavedBoards
}

@Composable
fun SavedGames(modifier: Modifier = Modifier) {
    // TODO: SavedGames
}

@Composable
private fun Statistics(modifier: Modifier = Modifier) {
    // TODO: Statistics
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfilePage() {
    ProvideCompositionalLocalsForPreview {
        ProfileScene(ProfileViewModel(), onClickBack = {}, onClickEdit = {})
    }
}