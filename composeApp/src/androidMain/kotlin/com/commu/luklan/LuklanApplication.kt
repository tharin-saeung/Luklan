package com.commu.luklan

import android.app.Application
import android.content.Context
import qrgenerator.AppContext

class LuklanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        INSTANCE = this
        AppContext.apply { set(applicationContext) }
    }

    companion object {
        lateinit var instance: LuklanApplication
            private set

        lateinit var INSTANCE: LuklanApplication
            private set

        fun getAppContext(): Context = instance.applicationContext
    }
}
