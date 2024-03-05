package org.keizar.android.ui.game.actions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

/**
 * The three winning counters shown at the top of the game board.
 */
@Composable
fun WinningCounter(
    winningCounter: Int,
    modifier: Modifier = Modifier
) {
    val flippedStates = remember { mutableStateListOf(true, true, true) }

    // Whenever winningCounter updates, set the corresponding token state to flipped
    LaunchedEffect(winningCounter) {
        if (winningCounter in 1..3) {
            flippedStates[winningCounter - 1] = false
        } else {
            for (i in 0 until 3) {
                flippedStates[i] = true
            }
        }
    }

    // A row of tokens
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Create a token for each number
        (1..3).forEach { number ->
            Token(
                number = number,
                isFlipped = flippedStates[number - 1]
            )

        }
    }
}

@Composable
fun Token(number: Int, isFlipped: Boolean) {
    val rotationDegrees by animateFloatAsState(targetValue = if (isFlipped) 180f else 0f)
    val paleGreen = Color(0xFFC8E6C9)
    val paleRed = Color(0xFFEB8C8C)
    Canvas(modifier = Modifier
        .size(48.dp)
        .padding(8.dp)
        .graphicsLayer {
            rotationY = rotationDegrees
            cameraDistance = 12f * density
        }) {
        val circleColor = if (rotationDegrees <= 90f) paleRed else paleGreen
        val radius = size.minDimension / 2
        val center = Offset(radius, radius)

        // Draw the token
        drawCircle(
            circleColor,
            radius,
            center
        )
        if (rotationDegrees > 90f) {
            val dotColor = Color.Black
            // Calculate positions for dots based on 'number'
            val dotPositions = getDotPositions(number, center, radius / 2)
            dotPositions.forEach { pos ->
                drawCircle(dotColor, radius / 6, pos)
            }
        }
    }
}


private fun getDotPositions(number: Int, center: Offset, distance: Float): List<Offset> {
    return when (number) {
        1 -> listOf(
            center // One dot in the center
        )

        2 -> listOf(
            Offset(center.x - distance / 3, center.y + distance / 2),
            Offset(center.x + distance / 3, center.y - distance / 2)
        )

        3 -> {
            val sideLength = distance * 2 / sqrt(3f)
            val horizontalDistance = sideLength / 2
            listOf(
                Offset(center.x, center.y - distance / 2), // Top vertex
                Offset(
                    center.x - horizontalDistance,
                    center.y + distance / 2
                ), // Bottom left vertex
                Offset(
                    center.x + horizontalDistance,
                    center.y + distance / 2
                )  // Bottom right vertex
            )
        }

        else -> emptyList()
    }
}
