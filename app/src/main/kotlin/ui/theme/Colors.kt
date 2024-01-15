package org.keizar.android.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color


/**
 * 把颜色稍微变浅一点
 */
@Composable
fun Color.slightlyWeaken(): Color {
    return copy(alpha = 1 - 0.38f)
}

/**
 * 把颜色变浅
 */
@Composable
fun Color.weaken(): Color {
    return copy(alpha = 0.5f)
}

/**
 * 把颜色变得很浅
 */
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