package org.keizar.android.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.keizar.client.KeizarClientFacade
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

@Composable
fun ProvideCompositionalLocalsForPreview(block: @Composable () -> Unit) {
    if (GlobalContext.getOrNull() == null) {
        GlobalContext.startKoin {
            modules(module {
                single<KeizarClientFacade> { KeizarClientFacade() }
            })
        }
    }

    CompositionLocalProvider {
        block()
    }
}