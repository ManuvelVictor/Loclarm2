package com.victor.loclarm2.presentation

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LoclarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext,"AIzaSyD0kACOietBw9It5g_iGNE2vrJrRnv-cmY")
        }
    }
}
