package org.keizar.android

import org.keizar.android.client.Client
import org.keizar.android.client.SessionManager
import org.keizar.android.data.SavedStateRepository
import org.keizar.android.persistent.RepositoryModules
import org.keizar.android.persistent.savedStateStore
import org.keizar.client.KeizarClientFacade
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

fun getKoinModule(client: Client = Client(BuildConfig.SERVER_ENDPOINT)): Module {
    return module {
        single<KeizarClientFacade> {
            KeizarClientFacade(
                BuildConfig.SERVER_ENDPOINT,
                get<SessionManager>().token
            )
        }
        single<SavedStateRepository> { SavedStateRepository(androidContext().savedStateStore) }
        single<SessionManager> { SessionManager() }
        single<Client> { client }
        includes(client.module)
        includes(RepositoryModules)
    }
}