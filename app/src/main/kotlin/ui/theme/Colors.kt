package org.keizar.android.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun Color.slightlyWeaken(): Color {
    return copy(alpha = 1 - 0.38f)
}

@Composable
fun Color.weaken(): Color {
    return copy(alpha = 0.5f)
}

@Composable
fun Color.stronglyWeaken(): Color {
    return copy(alpha = 0.38f)
}
