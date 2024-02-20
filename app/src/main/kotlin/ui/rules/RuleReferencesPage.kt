package org.keizar.android.ui.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.keizar.android.ui.game.Tile
import org.keizar.android.ui.game.TileImage
import org.keizar.game.TileType

@Composable
fun RuleReferencesScene(
    onClickBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Rule Book") },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                }
            )
        },
    ) { contentPadding ->
        Column(
            Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            RuleReferencesPage()
        }
    }
}

@Composable
fun RuleReferencesPage(
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = "Rules",
            Modifier.padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        Column(
            Modifier
                .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (rule in rules) {
                OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(text = rule)
                    }
                }
            }
        }

        Text(
            text = "Symbols",
            Modifier.padding(vertical = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        Symbols(Modifier.fillMaxWidth())
    }
}

@Composable
private fun Symbols(
    modifier: Modifier = Modifier,
) {
    val tiles = TileType.entries
    FlowRow(
        maxItemsInEachRow = 4,
        horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        for (tile in tiles) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Tile(onClick = { }, backgroundColor = Color.DarkGray, Modifier.size(64.dp)) {
                    TileImage(
                        tileType = tile,
                        Modifier.fillMaxSize(),
                    )
                }
                Text(
                    text = tile.displayName,
                    Modifier.padding(vertical = 3.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private val rules = listOf(
    "1. The players make one move per turn, white starts.",
    "2. All pieces start as either pawns on a plain tile or as the chess symbol they occupy.",
    "3. A piece assumes the moves and may capture the opponent's pieces according to the chess symbol it moves from regardless of the colour of the tile. (For example a white piece will move as queen from a white queen tile as well as from a black queen tile.)",
    "4. Pieces on plain tiles advance ahead or capture diagonally one square at a time as pawns. They may advance two squares from the first two rows, except the two black pieces in the KEIZÁR® coloumn (d7, d8) and the two adjacent pieces to them in the first row (c8, e8) and unless there is a symbol on the next square in front of them.",
    "5. Unlike in chess, there is no en passant move, castling, check or checkmate. The king has no greater value or role than any other symbol, it is only used to determine a move.\nOnce a piece reaches the opponent's first row on a plain tile it stays there unless it is captured, there is no promotion.",
    "6. To win, a piece must enter the KEIZÁR®tile. The opponent then has three moves to capture this piece while both players still keep moving in their turns. The piece must remain on the KEIZÁR® unless it is captured in three moves thus winning the game.",
    "7. If the KEIZÁR® is captured by the opponent the count of three moves starts again.",
    "8. You lose the game if none of your pieces can move, unless one of your piece is already on the KEIZÁR®, in which case you win."
)

@Composable
@Preview(showBackground = true)
private fun PreviewRuleReferencesPage() {
    RuleReferencesPage(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    )
}

@Composable
@Preview(showBackground = true)
private fun PreviewSymbols() {
    Symbols()
}