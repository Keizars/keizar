package org.keizar.android.ui.game.actions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.flatMapLatest
import org.keizar.android.ui.game.GameBoardViewModel
import org.keizar.android.ui.game.rememberSinglePlayerGameBoardForPreview
import org.keizar.game.Role

/**
 * An animated indicator text that shows current turn state.
 *
 * Displayed as "Your Turn" in primary color, and "Waiting" in light gray.
 */
@Composable
fun TurnStatusIndicator(
    vm: GameBoardViewModel,
    modifier: Modifier = Modifier
) {
    val selfRole by vm.selfRole.collectAsState()
    val curRole by vm.currentRound.flatMapLatest { it.curRole }.collectAsState(initial = Role.WHITE)
    val isPlayerTurn = selfRole == curRole


    // Animate the rotation
    val rotationY by animateFloatAsState(
        targetValue = if (isPlayerTurn) 0f else 180f,
        animationSpec = tween(durationMillis = 600), label = ""
    )


    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .widthIn(min = 90.dp)
            .height(36.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 8 * density
            }
            .background(
                color = if (rotationY <= 90f) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        if (rotationY <= 90f) {
            Text(
                text = "Your Turn",
                color = MaterialTheme.colorScheme.onPrimary,
                overflow = TextOverflow.Visible,
                softWrap = false,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            Text(
                text = "Waiting",
                color = Color.White,
                modifier = Modifier.graphicsLayer { this.rotationY = 180f },
                overflow = TextOverflow.Visible,
                softWrap = false,
            )
        }
    }
}

@Preview(showBackground = true, fontScale = 1f)
@Preview(showBackground = true, fontScale = 2f)
@Composable
fun TurnStatusIndicatorPreview() {
    val vm = rememberSinglePlayerGameBoardForPreview()
    TurnStatusIndicator(vm = vm)
}