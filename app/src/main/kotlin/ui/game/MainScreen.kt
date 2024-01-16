package org.keizar.android.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.keizar.game.BoardProperties

@Composable
fun MainScreen() {
    GameBoard(properties = remember {
        BoardProperties.getStandardProperties()
    })
}