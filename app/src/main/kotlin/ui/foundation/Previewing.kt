package org.keizar.android.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import org.keizar.android.getKoinModule
import org.keizar.android.ui.KeizarApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

@Composable
fun ProvideCompositionalLocalsForPreview(block: @Composable () -> Unit) {
    if (GlobalContext.getOrNull() == null) {
        val context = LocalContext.current
        GlobalContext.startKoin {
            androidContext(context)
            modules(getKoinModule())
        }
    }

    CompositionLocalProvider {
        KeizarApp {
            block()
        }
    }
}