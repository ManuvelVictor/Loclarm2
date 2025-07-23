package com.victor.loclarm2.geofence

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.victor.loclarm2.presentation.alarmRing.AlarmService

object AlarmTriggerHelper {
    fun triggerAlarm(context: Context, alarmId: String) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("alarmId", alarmId)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}