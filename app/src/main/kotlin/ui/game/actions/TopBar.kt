package org.keizar.android.ui.game.actions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.keizar.android.ui.game.GameBoardViewModel

@Composable
fun GameBoardTopBar(
    vm: GameBoardViewModel,
    turnStatusIndicator: (@Composable () -> Unit)? = {
        TurnStatusIndicator(vm, Modifier.padding(all = 6.dp))
    },
    winningCounter: (@Composable () -> Unit)? = {
        val counter by vm.winningCounter.collectAsState()
        WinningCounter(counter)
    },
) {
    Box(Modifier.fillMaxWidth()) {
        turnStatusIndicator?.let { it ->
            Box(modifier = Modifier.padding(all = 6.dp)) {
                it()
            }
        }
        winningCounter?.let {
            Box(modifier = Modifier.align(Alignment.Center)) {
                it()
            }
        }
    }
}
