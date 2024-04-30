package com.sp.app.mapgo.koin

import android.app.Activity
import android.app.Application
import android.os.Bundle
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level

class MyApplication: Application(), Application.ActivityLifecycleCallbacks {
    private var koinApplication: KoinApplication? = null

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

        initialize()
    }

    override fun onTerminate() {
        super.onTerminate()
        terminate()

        unregisterComponentCallbacks(this)
    }

    private fun initialize() {
        if(koinApplication == null) {
            koinApplication = startKoin {
                androidContext(applicationContext)
                androidLogger(Level.INFO)
                modules(repositoryModule, viewModelModule)

            }
        }
    }

    private fun terminate() {
        koinApplication?.close()
        koinApplication = null
        stopKoin()
    }

    /************************************************************
     *  implements Method [Application.ActivityLifecycleCallbacks]
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

}