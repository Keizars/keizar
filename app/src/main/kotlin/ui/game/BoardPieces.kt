package org.keizar.android.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.keizar.game.BoardProperties
import kotlin.random.Random


@Composable
fun BoardPieces(
    properties: BoardProperties,
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {

    }
}

@Composable
private fun Piece(
    modifier: Modifier = Modifier,
) {
//    contentColor = if (player == Player.BLACK) Color.Black else Color.White,

    Box {

    }
}

@Composable
private fun PlayerIconBox(
    modifier: Modifier = Modifier,
) {
//    Box(
//        modifier = Modifier
//            .matchParentSize()
//            .padding(10.dp)
//    ) {
//        PlayerIcon(
////                    contentDescription = if (it == Player.BLACK) "Player Black" else "Player White",
//            color = if (it == Player.BLACK) Color.Black else Color.White,
//            modifier = Modifier.matchParentSize()
//        )
//    }
}


@Composable
private fun PlayerIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .border(0.5.dp, Color.Gray, shape = CircleShape)
            .padding(all = 4.5.dp)
            .shadow(1.dp, CircleShape)
            .clip(CircleShape)
            .border(1.dp, Color.Gray, shape = CircleShape)
            .shadow(1.dp, CircleShape),
    ) {
        Spacer(
            Modifier
                .matchParentSize()
                .background(color),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBoardPiecesWithBackground() {
    BoxWithConstraints {
        val prop = remember {
            BoardProperties.getStandardProperties(Random(0))
        }
        val vm = remember {
            GameBoardViewModel(prop)
        }
        BoardBackground(
            prop,
            vm,
            Modifier.size(min(maxWidth, maxHeight))
        )
        BoardPieces(properties = prop, vm = vm)
    }
}