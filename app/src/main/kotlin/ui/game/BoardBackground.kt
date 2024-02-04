package org.keizar.android.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import org.keizar.game.BoardProperties
import org.keizar.game.GameSession
import org.keizar.game.Role
import org.keizar.game.TileType
import org.keizar.utils.communication.game.BoardPos
import org.keizar.utils.communication.game.Player

@Composable
fun BoardBackground(
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        BoardTiles(
            rotationDegrees = vm.boardTransitionController.boardBackgroundRotation,
            properties = vm.boardProperties,
            currentPick = vm.currentPick.collectAsStateWithLifecycle().value,
            onClickTile = { logicalPos -> vm.onClickTile(logicalPos) },
        )
        BoardTileLabels(
            properties = vm.boardProperties,
            pieceArranger = vm.pieceArranger,
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Composable
fun BoardTiles(
    rotationDegrees: Float,
    properties: BoardProperties,
    currentPick: Pick?,
    onClickTile: (logicalPos: BoardPos) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicBoardBackground(
        properties = properties,
        modifier = modifier.rotate(rotationDegrees),
    ) { pos ->
        val picked = remember(currentPick) {
            currentPick?.piece?.pos?.value == pos
        }
        Tile(
            onClick = { onClickTile(pos) },
            backgroundColor = tileBackgroundColor(picked, properties, pos.row, pos.col),
            Modifier.fillMaxSize(),
        ) {
            TileImage(
                tileType = run {
//                    val posUpdated by rememberUpdatedState(newValue = pos)
//                    val viewPos = snapshotFlow { posUpdated }
//                    val currPos by remember(properties) {
//                        pieceArranger.viewToLogical(viewPos)
//                    }.collectAsStateWithLifecycle(BoardPos(0, 0))
                    properties.tileArrangement[pos] ?: TileType.PLAIN
                },
                role = null,
                Modifier
                    .fillMaxSize()
                    .rotate(360 - rotationDegrees), // cancel rotation
            )
        }
    }
}

@Composable
fun BoardTileLabels(
    properties: BoardProperties,
    pieceArranger: PieceArranger,
    modifier: Modifier = Modifier,
) {
    BasicBoardBackground(properties, modifier.background(Color.Transparent)) { pos ->
        TileLabel(
            color = if (properties.tileBackgroundColor(pos.row, pos.col)) Color.White else Color.Black,
            row = pos.row,
            col = pos.col,
            pieceArranger,
        )
    }
}

@Composable
fun BasicBoardBackground(
    properties: BoardProperties,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(pos: BoardPos) -> Unit,
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
                        content(BoardPos(row = row, col = col))
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.TileLabel(
    color: Color,
    row: Int,
    col: Int,
    pieceArranger: PieceArranger,
) {
    CompositionLocalProvider(
        LocalContentColor provides color
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
                modifier = Modifier.Companion
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 4.dp, end = 4.dp),
                style = TextStyle(fontSize = 10.sp),
            )
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
                selfPlayer = Player.FirstWhitePlayer,
            ),

            Modifier.size(min(maxWidth, maxHeight))
        )
    }
}