package org.keizar.android

import android.app.Application
import kotlinx.coroutines.flow.firstOrNull
import org.keizar.android.data.SavedStateRepository
import org.keizar.android.data.SessionManager
import org.keizar.android.persistent.RepositoryModules
import org.keizar.android.persistent.savedStateStore
import org.keizar.client.AccessTokenProvider
import org.keizar.client.Client
import org.keizar.client.ClientConfig
import org.keizar.client.KeizarWebsocketClientFacade
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The main application class.
 *
 * Mainly used to start dependency injection modules with Koin.
 */
class KeizarApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@KeizarApplication)
            modules(createKoinModule())
        }
    }

    companion object {
        /**
         * Gets a Koin module for the Android app.
         *
         * # Dependency Injection in the App
         *
         * The app uses Koin for dependency injection.
         *
         * Koin is started in the very beginning of the app, on [KeizarApplication.onCreate].
         */
        private fun createKoinModule(
            client: Client,
        ): Module = module {
            includes(RepositoryModules) // data layer repositories

            single<KeizarWebsocketClientFacade> {
                KeizarWebsocketClientFacade(
                    BuildConfig.SERVER_ENDPOINT,
                    get<SessionManager>().token
                )
            }
            single<SavedStateRepository> { SavedStateRepository(androidContext().savedStateStore) }
            single<SessionManager> { SessionManager() }
            single<AccessTokenProvider> {
                SessionManagerAccessTokenProvider { get<SessionManager>() }
            }
            includes(client.servicesModule)
        }

        /**
         * Gets a Koin module for the Android app.
         *
         * # Dependency Injection in the App
         *
         * The app uses Koin for dependency injection.
         *
         * Koin is started in the very beginning of the app, on [KeizarApplication.onCreate].
         */
        fun createKoinModule(): Module {
            val client = Client(
                config = ClientConfig(
                    baseUrl = BuildConfig.SERVER_ENDPOINT,
                ),
            )
            return createKoinModule(client)
        }
    }
}

/**
 * Adapter for [SessionManager] to be used as an [AccessTokenProvider].
 */
internal class SessionManagerAccessTokenProvider(
    private val sessionManager: () -> SessionManager,
) : AccessTokenProvider {
    override suspend fun getAccessToken(): String? {
        return sessionManager().token.firstOrNull()
    }

    override suspend fun invalidateToken() {
        sessionManager().invalidateToken()
    }
}
