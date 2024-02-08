package org.keizar.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import org.keizar.client.KeizarClientFacade
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

class KeizarApplication : Application() {
    private var currentActivity: Activity? = null

    init {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                currentActivity = activity
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
                if (currentActivity == activity) {
                    currentActivity = null
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@KeizarApplication)
            modules(module {
                single<KeizarClientFacade> { KeizarClientFacade() }
            })
        }
    }
}
