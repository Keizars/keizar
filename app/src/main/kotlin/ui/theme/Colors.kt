package org.keizar.android.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
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

@Stable
fun myDarkColorTheme(): ColorScheme {
    PaletteTokens.run {
        return darkColorScheme(
            background = Neutral10,
            surface = Neutral20,
            surfaceVariant = NeutralVariant20,
        )
    }
}

@Stable
fun myLightColorTheme(): ColorScheme = lightColorScheme(
    background = Color(0xfff1f2f4),
    surface = Color.White,
    surfaceVariant = Color.White,
)