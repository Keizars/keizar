package org.keizar.android.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

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