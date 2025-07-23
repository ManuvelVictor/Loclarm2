package com.victor.loclarm2.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GEOFENCE_RECEIVER", "Received intent: ${intent.action}")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            Log.e("GEOFENCE_RECEIVER", "Error in geofencing event: ${geofencingEvent?.errorCode}")
            return
        }
        val transitionType = geofencingEvent.geofenceTransition
        Log.d("GEOFENCE_RECEIVER", "Transition type: $transitionType")
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            Log.d("GEOFENCE_RECEIVER", "Triggering geofences: ${triggeringGeofences?.size}")
            if (triggeringGeofences != null) {
                for (geofence in triggeringGeofences) {
                    val alarmId = geofence.requestId
                    Log.d("GEOFENCE_RECEIVER", "Triggering alarm for ID: $alarmId")
                    AlarmTriggerHelper.triggerAlarm(context, alarmId)
                }
            }
        }
    }
}

