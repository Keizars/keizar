package org.keizar.android.ui.game.configuration

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.keizar.android.ui.external.placeholder.placeholder
import org.keizar.android.ui.foundation.isSystemInLandscape
import org.keizar.android.ui.foundation.launchInBackground
import org.keizar.android.ui.game.BoardTileLabels
import org.keizar.android.ui.game.BoardTiles
import org.keizar.android.ui.game.transition.PieceArranger
import org.keizar.game.BoardProperties
import org.keizar.game.Difficulty
import org.keizar.game.Role
import org.keizar.game.Role.BLACK
import org.keizar.game.Role.WHITE

@Composable
fun GameConfigurationScene(
    onClickGoBack: () -> Unit,
    navController: NavController,
) {
    GameConfigurationPage(
        onClickGoBack = onClickGoBack,
        onClickStart = {
            val configuration = ProtoBuf.Default.encodeToHexString(GameStartConfiguration.serializer(), it)
            navController.navigate(navController.graph["game/single-player"].id, Bundle().apply {
                putString("configuration", configuration)
            })
        },
        onClickLogin = { navController.navigate("auth/login") }
    )
}

private val ROUND_CORNER_RADIUS = 12.dp

@Composable
private fun GameConfigurationPage(
    onClickGoBack: () -> Unit,
    onClickStart: (GameStartConfiguration) -> Unit,
    onClickLogin: () -> Unit
) {
    val vm = rememberGameConfigurationViewModel()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Player vs Computer") },
                navigationIcon = {
                    IconButton(onClick = onClickGoBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            Modifier
                .padding(contentPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isSystemInLandscape()) {
                GameConfigurationPageLandscape(vm, onClickStart, Modifier.fillMaxSize())
            } else {
                GameConfigurationPagePortrait(vm, onClickStart, onClickLogin, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun GameConfigurationPageLandscape(
    vm: GameConfigurationViewModel,
    onClickStart: (GameStartConfiguration) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        Row(
            Modifier
                .widthIn(max = (maxWidth * 0.618f).coerceAtLeast(800.dp))
//                .height(IntrinsicSize.Min)
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                BoardLayoutPreview(vm)
            }

            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                BoardSeedTextField(vm, Modifier.fillMaxWidth())

                PlayAsSelector(vm)

                val selected by vm.difficulty.collectAsStateWithLifecycle(null)
                DifficultySelector(selected, setDifficulty = { vm.setDifficulty(it) })

                Row(
                    Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .height(50.dp), horizontalArrangement = Arrangement.End
                ) {
                    StartButton(
                        { onClickStart(vm.configuration.value) },
                        Modifier
                            .padding(top = 10.dp)
                            .height(45.dp)
                    )
                }

            }
        }

    }
}

@Composable
fun GameConfigurationPagePortrait(
    vm: GameConfigurationViewModel,
    onClickStart: (GameStartConfiguration) -> Unit,
    onClickLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        LazyColumn(
            modifier = Modifier
                .weight(1f) // This makes the LazyColumn fill the available space, leaving space for the Button at the bottom
                .padding(horizontal = 16.dp)
        ) {
            item { BoardLayoutPreview(vm) }
            item { BoardSeedTextField(vm, Modifier.fillMaxWidth()) }
            item { PlayAsSelector(vm) }
            item {
                val selected by vm.difficulty.collectAsStateWithLifecycle(null)
                DifficultySelector(selected, setDifficulty = { vm.setDifficulty(it) })
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    vm.launchInBackground {
                        val isLoggedIn = vm.sessionManagerService.isLoggedIn.first()
                        if (isLoggedIn) {
                            vm.seedBankService.addSeed(vm.configurationSeed.first())
                        } else {
                            withContext(Dispatchers.Main) {
                                onClickLogin()
                            }
                        }
                    }
                }, modifier = Modifier
                    .padding(top = 10.dp)
                    .height(45.dp)
            ) {
                Text("Save Seed", modifier = Modifier, textAlign = TextAlign.Center)
            }

            StartButton(
                { onClickStart(vm.configuration.value) },
                Modifier
                    .padding(top = 10.dp)
                    .height(45.dp)
            )
        }
    }
}

@Composable
private fun StartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier
    ) {
        Text("Start", style = MaterialTheme.typography.bodyLarge)
        Icon(
            Icons.AutoMirrored.Default.ArrowForward,
            contentDescription = "Start",
            Modifier.padding(start = 4.dp)
        )
    }
}


@Composable
private fun BoardSeedTextField(
    vm: GameConfigurationViewModel,
    modifier: Modifier = Modifier,
) {
    val text by vm.configurationSeedText.collectAsStateWithLifecycle()
    val isError by vm.isConfigurationSeedTextError.collectAsStateWithLifecycle(false)
    val refreshEnable by vm.freshButtonEnable.collectAsStateWithLifecycle(true)

    BoardSeedTextField(
        text = text,
        onValueChange = { vm.setConfigurationSeedText(it) },
        onClickRandom = { vm.updateRandomSeed() },
        isError = isError,
        refreshEnable = refreshEnable,
        readOnly = false,
        modifier = modifier,
    )
}

@Composable
fun BoardSeedTextField(
    text: String,
    onValueChange: (String) -> Unit,
    onClickRandom: () -> Unit,
    isError: Boolean,
    refreshEnable: Boolean,
    readOnly: Boolean,
    modifier: Modifier = Modifier,
    supportingText: @Composable () -> Unit = {
        Text(text = "Explore new board layouts by changing the seed")
    },
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    OutlinedTextField(
        value = text,
        onValueChange = onValueChange,
        label = { Text(text = "Seed") },
        supportingText = {
            if (isError) {
                Text(text = "Error: Invalid seed. Please correct or regenerate one")
            } else {
                supportingText()
            }
        },
        isError = isError,
        readOnly = readOnly,
        trailingIcon = {
            if (clipboardManager.getText()?.text?.startsWith("P-") == true) {
                IconButton(onClick = {
                    onValueChange(clipboardManager.getText()?.text ?: "")
                    Toast.makeText(context, "Seed pasted", Toast.LENGTH_SHORT).show()
                }, enabled = refreshEnable) {
                    Icon(Icons.Default.ContentPaste, contentDescription = "Paste seed")
                }
            } else {
                IconButton(onClick = {
                    onClickRandom()
                }, enabled = refreshEnable) {
                    Icon(Icons.Default.Refresh, contentDescription = "Generate random seed")
                }
            }
        },
        shape = RoundedCornerShape(ROUND_CORNER_RADIUS),
        singleLine = true,
        modifier = modifier,
    )
}


@Composable
private fun BoardLayoutPreview(
    vm: GameConfigurationViewModel
) {
    BoardLayoutPreview(
        boardProperties = vm.boardProperties.collectAsStateWithLifecycle(null).value,
        playAs = vm.playAs.collectAsStateWithLifecycle(null).value,
        Modifier.padding(bottom = 16.dp)
    )
}


@Composable
fun BoardLayoutPreview(
    boardProperties: BoardProperties?,
    playAs: Role?,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        val boardSize = min(maxWidth, maxHeight)

        val sizeFactor = 1f
        val adjustedBoardSize = boardSize * sizeFactor
        Box(
            Modifier
                .size(adjustedBoardSize)
                .clip(RoundedCornerShape(4.dp))
                .placeholder(boardProperties == null || playAs == null),
        ) {
            boardProperties?.let { prop ->
                val pieceArranger = remember(prop) { PieceArranger(prop, snapshotFlow { playAs }.filterNotNull()) }
                BoardTiles(
                    rotationDegrees = if (playAs == WHITE) 0f else 180f,
                    properties = prop,
                    currentPick = null,
                    onClickTile = {},
                    Modifier.matchParentSize(),
                )
                BoardTileLabels(
                    properties = prop,
                    pieceArranger = pieceArranger,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}

@Composable
private fun PlayAsSelector(vm: GameConfigurationViewModel) {
    Text(
        text = "Play as",
        Modifier
            .padding(top = 16.dp)
            .padding(start = 16.dp),
        style = MaterialTheme.typography.labelMedium
    )

    val playAs by vm.playAs.collectAsStateWithLifecycle(null)
    SingleChoiceSegmentedButtonRow(
        Modifier
            .padding(top = 4.dp)
            .placeholder(playAs == null)
            .fillMaxWidth()
    ) {
        SegmentedButton(
            selected = playAs == BLACK,
            onClick = { vm.setPlayAs(BLACK) },
            shape = RoundedCornerShape(topStart = ROUND_CORNER_RADIUS, bottomStart = ROUND_CORNER_RADIUS),
        ) {
            Text(renderPlayAs(BLACK))
        }
//        SegmentedButton(
//            selected = playAs == null,
//            onClick = { vm.setPlayAs(null) },
//            shape = RectangleShape,
//        ) {
//            Text(renderPlayAs(null))
//        }
        SegmentedButton(
            selected = playAs == WHITE,
            onClick = { vm.setPlayAs(WHITE) },
            shape = RoundedCornerShape(topEnd = ROUND_CORNER_RADIUS, bottomEnd = ROUND_CORNER_RADIUS),
        ) {
            Text(renderPlayAs(WHITE))
        }
    }
}

@Composable
private fun DifficultySelector(
    selected: Difficulty?,
    setDifficulty: (Difficulty) -> Unit,
) {
    Text(
        text = "Difficulty",
        Modifier
            .padding(top = 16.dp)
            .padding(start = 16.dp),
        style = MaterialTheme.typography.labelMedium
    )

    SingleChoiceSegmentedButtonRow(
        Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
    ) {
        SegmentedButton(
            selected = selected == Difficulty.EASY,
            onClick = { setDifficulty(Difficulty.EASY) },
            shape = RoundedCornerShape(topStart = ROUND_CORNER_RADIUS, bottomStart = ROUND_CORNER_RADIUS),
        ) {
            Text("Easy")
        }
        SegmentedButton(
            selected = selected == Difficulty.MEDIUM,
            onClick = { setDifficulty(Difficulty.MEDIUM) },
            shape = RectangleShape,
            enabled = true,
        ) {
            Text("Medium")
        }
        SegmentedButton(
            selected = selected == Difficulty.HARD,
            onClick = { setDifficulty(Difficulty.HARD) },
            shape = RoundedCornerShape(topEnd = ROUND_CORNER_RADIUS, bottomEnd = ROUND_CORNER_RADIUS),
            enabled = false,
            colors = SegmentedButtonDefaults.colors(
                disabledInactiveContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
            )
        ) {
            Text("Hard")
        }
    }
}

@Preview
@Composable
private fun PreviewDifficultySelector() {
    DifficultySelector(Difficulty.EASY) {}
}

@Stable
private fun renderPlayAs(entry: Role?) = when (entry) {
    BLACK -> "Black"
    WHITE -> "White"
    null -> "Random"
}


@Preview(heightDp = 800, device = Devices.PHONE)
@Preview(heightDp = 800, device = Devices.TABLET)
@Composable
private fun PreviewGameConfigurationPage() {
    GameConfigurationPage(
        onClickGoBack = {},
        onClickStart = {},
        onClickLogin = {}
    )
}