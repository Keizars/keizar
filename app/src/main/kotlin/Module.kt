package org.keizar.android

import org.keizar.android.data.SavedStateRepository
import org.keizar.android.persistent.savedStateStore
import org.keizar.client.KeizarClientFacade
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

fun getKoinModule(): Module {
    return module {
        single<KeizarClientFacade> { KeizarClientFacade(BuildConfig.SERVER_ENDPOINT) }
        single<SavedStateRepository> { SavedStateRepository(androidContext().savedStateStore) }
    }
}