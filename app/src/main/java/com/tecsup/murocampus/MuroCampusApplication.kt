package com.tecsup.murocampus

import android.app.Application
import com.google.firebase.FirebaseApp

open class MuroCampusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
