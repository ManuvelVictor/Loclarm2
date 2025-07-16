package com.victor.loclarm2.data.geofence

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class LocationCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun doWork(): Result {
        val alarmId = inputData.getString("alarmId") ?: return Result.failure()
        val targetLat = inputData.getDouble("latitude", 0.0)
        val targetLng = inputData.getDouble("longitude", 0.0)
        val radius = inputData.getFloat("radius", 100f)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        return try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                val result = FloatArray(1)
                Location.distanceBetween(
                    it.latitude,
                    it.longitude,
                    targetLat,
                    targetLng,
                    result
                )
                val distance = result[0]

                if (distance <= radius) {
                    NotificationUtils.showNotification(context, "Loclarm", "You're in range of alarm: $alarmId")
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
