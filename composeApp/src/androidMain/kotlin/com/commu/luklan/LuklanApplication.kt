package com.commu.luklan

import android.app.Application
import android.content.Context

class LuklanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: LuklanApplication
            private set

        fun getAppContext(): Context = instance.applicationContext
    }
}
