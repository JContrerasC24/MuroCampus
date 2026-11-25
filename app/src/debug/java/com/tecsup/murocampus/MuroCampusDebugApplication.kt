package com.tecsup.murocampus

import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class MuroCampusDebugApplication : MuroCampusApplication() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.getApps(this).firstOrNull()?.let { app ->
            FirebaseAppCheck.getInstance(app)
                .installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        }
    }
}
