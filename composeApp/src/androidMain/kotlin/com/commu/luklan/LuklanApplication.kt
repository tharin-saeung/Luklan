package com.commu.luklan

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import qrgenerator.AppContext

class LuklanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        // Initialize App Check for debug builds
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

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
