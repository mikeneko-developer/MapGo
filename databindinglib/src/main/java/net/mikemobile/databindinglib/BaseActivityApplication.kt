package net.mikemobile.databindinglib

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import android.os.Bundle
import android.app.Activity
import net.mikemobile.databindinglib.base.ActivityNavigator
import net.mikemobile.databindinglib.base.BaseFragmentFactory
import org.koin.core.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.core.module.Module

abstract class BaseActivityApplication : Application(), Application.ActivityLifecycleCallbacks {
    abstract fun onFragmentFractory(): BaseFragmentFactory

    abstract fun onViewModule(): Module
    abstract fun onNavigatorModule(): Module
    abstract fun onModelModule(): Module
    abstract fun onOtherModule(): Module




    private var activitylist: MutableList<Activity> = mutableListOf()
    private var koinApplication: KoinApplication? = null


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

    }

    fun finishActicities() {
        activitylist.forEach { it.finishAndRemoveTask() }
    }

    fun initialize() {
        // make koin container
        if(koinApplication == null) {
            koinApplication = startKoin {
                androidContext(applicationContext)
                modules(onViewModule(), onNavigatorModule(),onModelModule(),onOtherModule())
            }
        }
    }

    fun terminate() {
        val list = ArrayList<Module>()
        list.add(onViewModule())
        list.add(onNavigatorModule())
        list.add(onModelModule())
        list.add(onOtherModule())

        //koinApplication?.unloadModules(list)
        koinApplication?.close()
        koinApplication = null
        stopKoin()
        onFragmentFractory().destroyInstance()

        this.onTerminate()
    }

    /************************************************************
     *  implements Method [Application.ActivityLifecycleCallbacks]
     */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activitylist.add(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        activitylist.remove(activity)
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}
}