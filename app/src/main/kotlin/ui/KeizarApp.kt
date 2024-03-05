package org.keizar.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.keizar.android.ui.theme.myDarkColorTheme
import org.keizar.android.ui.theme.myLightColorTheme

/**
 * The root composable function to provide theming and necessary [CompositionLocal] values for running the app in production.
 *
 * It also provides the features:
 * - Adds a uniform background color to the whole app.
 * - Clear focus and hide keyboard when clicking any background area.
 */
@Composable
inline fun KeizarApp(
    colorScheme: ColorScheme = if (isSystemInDarkTheme()) myDarkColorTheme() else myLightColorTheme(),
    crossinline content: @Composable () -> Unit
) {
    CompositionLocalProvider {
        val focusManager by rememberUpdatedState(LocalFocusManager.current)
        val keyboard by rememberUpdatedState(LocalSoftwareKeyboardController.current)
        MaterialTheme(colorScheme) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .focusable(false)
                    .clickable(
                        remember { MutableInteractionSource() },
                        null,
                    ) {
                        keyboard?.hide()
                        focusManager.clearFocus()
                    }
                    .fillMaxSize()
            ) {
                content()
            }
        }
    }
}
