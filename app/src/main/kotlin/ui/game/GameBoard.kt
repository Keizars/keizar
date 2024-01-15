package org.keizar.android.ui.game

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
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
    val vm = remember { GameBoardViewModel() }
    Column(modifier) {
        for (row in properties.height - 1 downTo 0) {
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (col in 0..<properties.height) {
                    Tile(
                        backgroundColor = if (properties.tileBackgroundColor(row, col)) Color.Black else Color.White,
                        contentColor = if (!properties.tileBackgroundColor(row, col)) Color.Black else Color.White,
                        Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    ) {
//                        remember {
//                            DraggableAnchors<BoardPos> {
//
//                            }
//                        }

                        TileImage(
                            type = remember(properties) {
                                val currPos = BoardPos(row, col)
                                properties.tileTypes[currPos] ?: PLAIN
                            },
                            Modifier
                                .padding(4.dp)
                                .fillMaxSize()
//                                .anchoredDraggable(remember {
//                                    AnchoredDraggableState(
//                                        initialValue =
//                                    )
//                                }),
                        )
                    }
                }
            }
        }
    }
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
fun TileImage(type: TileType, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        when (type) {
            KING -> Icon(Icons.Default.Hiking, "King", Modifier.matchParentSize())
            QUEEN -> Icon(Icons.Default.Queue, "Queen", Modifier.matchParentSize())
            BISHOP -> Icon(Icons.Default.ShoppingCart, "Bishop", Modifier.matchParentSize())
            KNIGHT -> Icon(Icons.Default.NightsStay, "Knight", Modifier.matchParentSize())
            ROOK -> Icon(Icons.Default.Room, "Rook", Modifier.matchParentSize())
            KEIZAR -> Icon(Icons.Default.Key, "Keizar", Modifier.matchParentSize())
            PLAIN -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewGameBoard() {
    BoxWithConstraints {
        GameBoard(
            remember {
                BoardProperties.random(Random(100))
            },
            Modifier.size(min(maxWidth, maxHeight))
        )
    }
}