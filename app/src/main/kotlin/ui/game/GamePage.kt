package org.keizar.android.ui.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.keizar.game.BoardProperties
import kotlin.random.Random

@Composable
fun GamePage(
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        GameBoard(
            properties = BoardProperties.getStandardProperties(Random(0)),
            modifier = Modifier.fillMaxSize()
        )
    }
}