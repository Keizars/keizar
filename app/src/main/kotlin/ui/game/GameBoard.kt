package org.keizar.android.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.keizar.android.R
import org.keizar.android.ui.theme.slightlyWeaken
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.Player
import org.keizar.game.TileType
import org.keizar.game.TileType.BISHOP
import org.keizar.game.TileType.KEIZAR
import org.keizar.game.TileType.KING
import org.keizar.game.TileType.KNIGHT
import org.keizar.game.TileType.PLAIN
import org.keizar.game.TileType.QUEEN
import org.keizar.game.TileType.ROOK
import kotlin.random.Random


@Composable
fun GameBoard(
    properties: BoardProperties,
    modifier: Modifier = Modifier,
) {
    val vm = remember(properties) { GameBoardViewModel(properties) }
    Column(modifier) {
        for (row in properties.height - 1 downTo 0) {
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (col in 0..<properties.width) {
                    val currentPick by vm.currentPick.collectAsState()
                    val picked = remember(currentPick) {
                        currentPick?.pos == BoardPos(row, col)
                    }

                    val player by remember(properties) {
                        val currPos = BoardPos(row, col)
                        vm.pieces[currPos]!!
                    }.player.collectAsState(null)

                    Tile(
                        backgroundColor = tileBackgroundColor(picked, properties, row, col),
                        contentColor = if (player == Player.BLACK) Color.Black else Color.White,
                        Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    ) {
                        TileImage(
                            tileType = remember(properties) {
                                val currPos = BoardPos(row, col)
                                properties.tileArrangement[currPos] ?: PLAIN
                            },
                            player = player,
                            Modifier
                                .clickable { vm.onClick(BoardPos(row, col)) }
                                .padding(4.dp)
                                .fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun tileBackgroundColor(
    picked: Boolean,
    properties: BoardProperties,
    row: Int,
    col: Int
) = when {
    picked -> if (properties.tileBackgroundColor(row, col)) Color(0xff8bc34a) else Color(0xffc5e1a5)
    properties.tileBackgroundColor(row, col) -> Color(0xff6d9a4a)
    else -> Color(0xffe8edc8)
}

@Composable
fun Tile(
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier.background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun TileImage(
    tileType: TileType,
    player: Player?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.slightlyWeaken()) {
            when (tileType) {
                KING -> Icon(painter = painterResource(id = R.drawable.king), contentDescription = "King", Modifier.matchParentSize(), tint = Color.Unspecified)
                QUEEN -> Icon(painter = painterResource(id = R.drawable.queen), contentDescription = "Queen", Modifier.matchParentSize(), tint = Color.Unspecified)
                BISHOP -> Icon(painter = painterResource(id = R.drawable.bishop), contentDescription = "Bishop", Modifier.matchParentSize(), tint = Color.Unspecified)
                KNIGHT -> Icon(painter = painterResource(id = R.drawable.knight), contentDescription = "Knight", Modifier.matchParentSize(), tint = Color.Unspecified)
                ROOK -> Icon(painter = painterResource(id = R.drawable.rook), contentDescription = "Rook", Modifier.matchParentSize(), tint = Color.Unspecified)
                KEIZAR -> Icon(painter = painterResource(id = R.drawable.keizar), contentDescription = "Keizar", Modifier.matchParentSize(), tint = Color.Unspecified)
                PLAIN -> {}
            }
        }
        player?.let {
            when (it) {
                Player.BLACK -> Icon(
                    Icons.Default.Circle,
                    "Player Black",
                    Modifier.matchParentSize()
                )

                Player.WHITE -> Icon(
                    Icons.Default.Circle,
                    "Player White",
                    Modifier.matchParentSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewGameBoard() {
    BoxWithConstraints {
        GameBoard(
            remember {
                BoardProperties.getStandardProperties(Random(100))
            },
            Modifier.size(min(maxWidth, maxHeight))
        )
    }
}