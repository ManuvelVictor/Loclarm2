package com.victor.loclarm2.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun requestForegroundServiceLocationPermission(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val hasPermission = ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.FOREGROUND_SERVICE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.FOREGROUND_SERVICE_LOCATION),
                101
            )
        }
    }
}
