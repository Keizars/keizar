package org.keizar.android

import org.keizar.android.client.Client
import org.keizar.android.client.SessionManager
import org.keizar.android.data.SavedStateRepository
import org.keizar.android.persistent.RepositoryModules
import org.keizar.android.persistent.savedStateStore
import org.keizar.client.KeizarWebsocketClientFacade
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Gets a Koin module for the Android app.
 *
 * # Dependency Injection in the App
 *
 * The app uses Koin for dependency injection.
 *
 * Koin is started in the very beginning of the app, [KeizarApplication.onCreate].
 *
 * ## Retrieving
 */
fun getKoinModule(client: Client = Client(BuildConfig.SERVER_ENDPOINT)): Module {
    return module {
        single<KeizarWebsocketClientFacade> {
            KeizarWebsocketClientFacade(
                BuildConfig.SERVER_ENDPOINT,
                get<SessionManager>().token
            )
        }
        single<SavedStateRepository> { SavedStateRepository(androidContext().savedStateStore) }
        single<SessionManager> { SessionManager() }
        includes(client.module)
        includes(RepositoryModules)
    }
}