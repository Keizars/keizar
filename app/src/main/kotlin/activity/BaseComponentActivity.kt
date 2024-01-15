package org.keizar.android.activity

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import org.keizar.android.ui.theme.myDarkColorTheme
import org.keizar.android.ui.theme.myLightColorTheme

abstract class BaseComponentActivity : ComponentActivity() {
    val currentColorScheme
        @Composable
        get() = if (isSystemInDarkTheme()) {
            myDarkColorTheme()
        } else {
            myLightColorTheme()
        }

    fun enableDrawingToSystemBars() {
        enableEdgeToEdge(
            SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
