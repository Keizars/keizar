package org.keizar.android.ui.foundation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import org.keizar.android.KeizarApplication
import org.keizar.android.ui.KeizarApp
import org.keizar.android.ui.theme.myDarkColorTheme
import org.keizar.android.ui.theme.myLightColorTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

/**
 * The root composable function in each preview function to provide the necessary [CompositionLocal] values and theming.
 *
 * All `@Preview` functions should use this function so that you see correct themes.
 */
@Composable
fun ProvideCompositionalLocalsForPreview(
    colorScheme: ColorScheme = if (isSystemInDarkTheme()) myDarkColorTheme() else myLightColorTheme(),
    block: @Composable () -> Unit
) {
    if (GlobalContext.getOrNull() == null) {
        val context = LocalContext.current
        GlobalContext.startKoin {
            androidContext(context)
            modules(KeizarApplication.createKoinModule())
        }
    }

    CompositionLocalProvider {
        KeizarApp(colorScheme) {
            block()
        }
    }
}