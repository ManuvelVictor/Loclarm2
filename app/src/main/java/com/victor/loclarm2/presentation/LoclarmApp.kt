package com.victor.loclarm2.presentation

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.victor.loclarm2.R
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LoclarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.GOOGLE_MAPS_API_KEY))
        }
    }
}
