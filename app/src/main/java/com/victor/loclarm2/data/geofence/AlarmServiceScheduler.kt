package com.victor.loclarm2.data.geofence

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.victor.loclarm2.data.model.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class AlarmServiceScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun scheduleLocationWorker(alarm: Alarm) {
        val workRequest = OneTimeWorkRequestBuilder<LocationCheckWorker>()
            .setInputData(
                workDataOf(
                    "alarmId" to alarm.id,
                    "latitude" to alarm.latitude,
                    "longitude" to alarm.longitude,
                    "radius" to alarm.radius
                )
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}