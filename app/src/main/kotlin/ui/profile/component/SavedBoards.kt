package org.keizar.android.ui.profile.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.game.BoardTiles
import org.keizar.android.ui.profile.ProfileViewModel
import org.keizar.android.ui.profile.SavedSeed
import org.keizar.game.BoardProperties


@Composable
fun SavedBoards(vm: ProfileViewModel, modifier: Modifier = Modifier, onClickPlayGame: (String) -> Unit) {
    if (vm.isLoadingSeeds.value) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text(text = "Loading Boards")
        }
    }
    val allSeeds by vm.allSeeds.collectAsState()
    if (!isSystemInLandscape()) {
        SavedBoardCardsSummary(
            modifier = Modifier.fillMaxWidth(),
            vm = vm,
            allSeeds = allSeeds,
            onClickPlayGame = onClickPlayGame
        )
    } else {
        Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
            val selectedSeed by vm.selectedSeed.collectAsStateWithLifecycle()
            SavedBoardCardsSummary(
                modifier = Modifier.wrapContentSize(),
                vm = vm,
                allSeeds = allSeeds,
                onClickPlayGame = onClickPlayGame
            )

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
private fun SavedBoardCardsSummary(
    modifier: Modifier = Modifier,
    vm: ProfileViewModel,
    allSeeds: List<SavedSeed>,
    onClickPlayGame: (String) -> Unit
) {
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
                            val clip =
                                ClipData.newPlainText("Copied seed", savedSeed.configurationSeed)
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
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        modifier = Modifier.align(Alignment.End),
                        onClick = { onClickPlayGame(savedSeed.configurationSeed) }) {
                        Text(text = "Play")
                    }
                }
            }
        }
    }
}
