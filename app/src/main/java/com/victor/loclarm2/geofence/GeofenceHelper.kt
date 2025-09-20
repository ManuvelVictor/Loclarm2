package com.victor.loclarm2.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GeofenceHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val geofencingClient = LocationServices.getGeofencingClient(context)

    fun addGeofence(latLng: LatLng, radius: Float, id: String, pendingIntent: PendingIntent) {
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("GeofenceHelper", "Location permissions not granted!")
            return
        }

        geofencingClient.addGeofences(request, getPendingIntent())
            .addOnSuccessListener {
                Log.d("GeofenceHelper", "Geofence added successfully: $id")
            }
            .addOnFailureListener { e ->
                Log.e("GeofenceHelper", "Failed to add geofence: ${e.message}")
            }
    }

    fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}