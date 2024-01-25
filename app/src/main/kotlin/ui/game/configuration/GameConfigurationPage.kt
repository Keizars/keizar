package org.keizar.android.ui.game.configuration

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.get
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.keizar.android.ui.external.placeholder.placeholder
import org.keizar.android.ui.game.BoardBackground
import org.keizar.android.ui.game.PieceArranger
import org.keizar.game.Difficulty
import org.keizar.game.Role
import org.keizar.game.Role.BLACK
import org.keizar.game.Role.WHITE

@Composable
fun GameConfigurationScene(
    navController: NavController,
) {
    GameConfigurationPage(
        onClickGoBack = { navController.popBackStack() },
        onClickStart = {
            val configuration = ProtoBuf.Default.encodeToHexString(GameStartConfiguration.serializer(), it)
            navController.navigate(navController.graph["game"].id, Bundle().apply {
                putString("configuration", configuration)
            })
        },
    )
}

private val ROUND_CORNER_RADIUS = 12.dp

@Composable
private fun GameConfigurationPage(
    onClickGoBack: () -> Unit,
    onClickStart: (GameStartConfiguration) -> Unit,
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
            LazyColumn(
                modifier = Modifier
                    .weight(1f) // This makes the LazyColumn fill the available space, leaving space for the Button at the bottom
                    .padding(horizontal = 16.dp)
            ) {
                item { BoardLayoutPreview(vm) }
                item { BoardSeedTextField(vm) }
                item { PlayAsSelector(vm) }
                item { DifficultySelector(vm) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            Row(Modifier.fillMaxWidth().height(50.dp), horizontalArrangement = Arrangement.End) {
                Button(onClick = { onClickStart(vm.configuration.value) }, Modifier.padding(top = 10.dp).height(45.dp)) {
                    Text("Start", style = MaterialTheme.typography.bodyLarge)
                    Icon(
                        Icons.AutoMirrored.Default.ArrowForward,
                        contentDescription = "Start",
                        Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardSeedTextField(
    vm: GameConfigurationViewModel,
) {
    val text by vm.configurationSeedText.collectAsStateWithLifecycle()
    OutlinedTextField(
        value = text,
        onValueChange = { vm.setConfigurationSeedText(it) },
        label = { Text(text = "Seed") },
        supportingText = { Text(text = "Explore new board layouts by changing the seed") },
        trailingIcon = {
            IconButton(onClick = { vm.updateRandomSeed() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Generate random seed")
            }
        },
        shape = RoundedCornerShape(ROUND_CORNER_RADIUS),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun BoardLayoutPreview(vm: GameConfigurationViewModel) {
    BoxWithConstraints {
        val boardProperties = vm.boardProperties.collectAsStateWithLifecycle(null).value
        val boardSize = min(maxWidth, maxHeight)

        val sizeFactor = 1f
        val adjustedBoardSize = boardSize * sizeFactor
        Box(
            Modifier
                .padding(bottom = 16.dp)
                .size(adjustedBoardSize)
                .clip(RoundedCornerShape(ROUND_CORNER_RADIUS))
                .placeholder(boardProperties == null),
        ) {
            boardProperties?.let { prop ->
                BoardBackground(
                    remember(prop, vm) { PieceArranger(prop, vm.playAs.filterNotNull()) },
                    properties = prop,
                    currentPick = null,
                    onClickTile = {},
                    Modifier.matchParentSize(),
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

    SingleChoiceSegmentedButtonRow(
        Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
    ) {
        val playAs by vm.playAs.collectAsStateWithLifecycle(null)
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
private fun DifficultySelector(vm: GameConfigurationViewModel) {
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
        val selected by vm.difficulty.collectAsStateWithLifecycle(Difficulty.EASY)
        SegmentedButton(
            selected = selected == Difficulty.EASY,
            onClick = { vm.setDifficulty(Difficulty.EASY) },
            shape = RoundedCornerShape(topStart = ROUND_CORNER_RADIUS, bottomStart = ROUND_CORNER_RADIUS),
        ) {
            Text("Easy")
        }
        SegmentedButton(
            selected = selected == Difficulty.MEDIUM,
            onClick = { vm.setDifficulty(Difficulty.MEDIUM) },
            shape = RectangleShape,
            enabled = false,
        ) {
            Text("Medium")
        }
        SegmentedButton(
            selected = selected == Difficulty.HARD,
            onClick = { vm.setDifficulty(Difficulty.HARD) },
            shape = RoundedCornerShape(topEnd = ROUND_CORNER_RADIUS, bottomEnd = ROUND_CORNER_RADIUS),
            enabled = false,
        ) {
            Text("Hard")
        }
    }
}

@Stable
private fun renderPlayAs(entry: Role?) = when (entry) {
    BLACK -> "Black"
    WHITE -> "White"
    null -> "Random"
}


@Preview(heightDp = 400)
@Composable
private fun PreviewGameConfigurationPage() {
    GameConfigurationPage(
        onClickGoBack = {},
        onClickStart = {},
    )
}