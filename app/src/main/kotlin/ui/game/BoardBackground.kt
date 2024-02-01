package org.keizar.android.ui.game

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import org.keizar.android.R
import org.keizar.android.ui.theme.slightlyWeaken
import org.keizar.game.BoardPos
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.Player
import org.keizar.game.Role
import org.keizar.game.TileType

@Composable
fun BoardBackground(
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier,
) {
    BoardBackground(
        vm.pieceArranger,
        properties = vm.boardProperties,
        currentPick = vm.currentPick.collectAsStateWithLifecycle().value,
        onClickTile = { vm.onClickTile(it) },
        modifier,
    )
}

@Composable
fun BoardBackground(
    pieceArranger: PieceArranger,
    properties: BoardProperties,
    currentPick: Pick?,
    onClickTile: (BoardPos) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        for (row in properties.height - 1 downTo 0) {
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (col in 0..<properties.width) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    ) {
                        val picked = remember(currentPick) {
                            currentPick?.viewPos == BoardPos(row, col)
                        }
                        Tile(
                            onClick = { onClickTile(BoardPos(row, col)) },
                            backgroundColor = tileBackgroundColor(picked, properties, row, col),
                            Modifier
                                .fillMaxSize(),
                        ) {
                            TileImage(
                                tileType = kotlin.run {
                                    val viewPos = snapshotFlow { BoardPos(row, col) }
                                    val currPos by remember(properties) {
                                        pieceArranger.viewToLogical(viewPos)
                                    }.collectAsStateWithLifecycle(BoardPos(0, 0))
                                    properties.tileArrangement[currPos] ?: TileType.PLAIN
                                },
                                role = null,
                                Modifier
                                    .fillMaxSize(),
                            )
                        }

                        CompositionLocalProvider(
                            LocalContentColor provides
                                    if (properties.tileBackgroundColor(row, col)) Color.White else Color.Black
                        ) {
                            if (col == 0) {
                                Text(
                                    text = remember(row) {
                                        pieceArranger.logicalToView(BoardPos(row = row, col = 0))
                                            .map { (it.row + 1).toString() }
                                    }.collectAsStateWithLifecycle(initialValue = "").value,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(top = 4.dp, start = 4.dp),
                                    style = TextStyle(fontSize = 10.sp),
                                )
                            }

                            if (row == 0) {
                                Text(
                                    text = remember(col) {
                                        pieceArranger.logicalToView(BoardPos(row = 0, col = col))
                                            .map { ('a' + it.col).toString() }
                                    }.collectAsStateWithLifecycle(initialValue = "").value,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(bottom = 4.dp, end = 4.dp),
                                    style = TextStyle(fontSize = 10.sp),
                                )
                            }
                        }
                    }


//                    val currentPick by vm.currentPick.collectAsState()
//                    val picked = remember(currentPick) {
//                        currentPick?.pos == BoardPos(row, col)
//                    }
//
//                    val player by remember(properties) {
//                        val currPos = BoardPos(row, col)
//                        vm.pieces[currPos]!!
//                    }.player.collectAsState(null)
//
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .fillMaxSize()
//                    ) {
//                        Tile(
//                            backgroundColor = tileBackgroundColor(picked, properties, row, col),
//                            contentColor = if (player == Player.BLACK) Color.Black else Color.White,
//                            Modifier
//                                .fillMaxSize(),
//                        ) {
//                            TileImage(
//                                tileType = remember(properties) {
//                                    val currPos = BoardPos(row, col)
//                                    properties.tileArrangement[currPos] ?: PLAIN
//                                },
//                                player = player,
//                                Modifier
//                                    .clickable { vm.onClick(BoardPos(row, col)) }
//                                    .fillMaxSize(),
//                            )
//                        }
//
//                        if (col == 0) {
//                            Text(
//                                text = (row + 1).toString(),
//                                modifier = Modifier
//                                    .align(Alignment.TopStart)
//                                    .padding(top = 2.dp, start = 2.dp),
//                                style = TextStyle(fontSize = 10.sp),
//                                color = Color.Black
//                            )
//                        }
//
//                        if (row == 0) {
//                            Text(
//                                text = ('a' + col).toString(),
//                                modifier = Modifier
//                                    .align(Alignment.BottomEnd)
//                                    .padding(bottom = 2.dp, end = 2.dp),
//                                style = TextStyle(fontSize = 10.sp),
//                                color = Color.Black
//                            )
//                        }
//                    }
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
): Color {
    return when {
        picked -> if (properties.tileBackgroundColor(row, col)) Color(0xff8bc34a) else Color(0xffc5e1a5)
        properties.tileBackgroundColor(row, col) -> Color.DarkGray
        else -> Color(0xFFefefef) // white
    }
//    return when {
//        picked -> if (properties.tileBackgroundColor(row, col)) Color(0xff8bc34a) else Color(0xffc5e1a5)
//        properties.tileBackgroundColor(row, col) -> Color(0xff6d9a4a)
//        else -> Color(0xffe8edc8)
//    }
}

@Composable
fun Tile(
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier,
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
fun TileImage(
    tileType: TileType,
    role: Role?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .then(if (tileType != TileType.PLAIN) Modifier.border(2.dp, color = Color(0xFFa800d4)) else Modifier)
    ) {
        CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.slightlyWeaken()) {
            when (tileType) {
                TileType.KING -> Icon(
                    painter = painterResource(id = R.drawable.king),
                    contentDescription = "King",
                    Modifier.matchParentSize(),
                    tint = Color.Unspecified
                )

                TileType.QUEEN -> Icon(
                    painter = painterResource(id = R.drawable.queen),
                    contentDescription = "Queen",
                    Modifier.matchParentSize(),
                    tint = Color.Unspecified
                )

                TileType.BISHOP -> Icon(
                    painter = painterResource(id = R.drawable.bishop),
                    contentDescription = "Bishop",
                    Modifier.matchParentSize(),
                    tint = Color.Unspecified
                )

                TileType.KNIGHT -> Icon(
                    painter = painterResource(id = R.drawable.knight),
                    contentDescription = "Knight",
                    Modifier.matchParentSize(),
                    tint = Color.Unspecified
                )

                TileType.ROOK -> Icon(
                    painter = painterResource(id = R.drawable.rook),
                    contentDescription = "Rook",
                    Modifier.matchParentSize(),
                    tint = Color.Unspecified
                )

                TileType.KEIZAR -> Icon(
                    painter = painterResource(id = R.drawable.keizar),
                    contentDescription = "Keizar",
                    Modifier.matchParentSize(),
                    tint = Color.Unspecified
                )

                TileType.PLAIN -> {}
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewBoardBackground() {
    BoxWithConstraints {
        val prop = remember {
            BoardProperties.getStandardProperties(0)
        }
        BoardBackground(
            rememberGameBoardViewModel(
                game = GameSession.create(prop),
                selfPlayer = Player.Player1,
            ),

            Modifier.size(min(maxWidth, maxHeight))
        )
    }
}