package org.keizar.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

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
            modules(getKoinModule())
        }
    }
}
