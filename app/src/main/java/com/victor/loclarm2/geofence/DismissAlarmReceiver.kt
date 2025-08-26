package com.victor.loclarm2.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.victor.loclarm2.geofence.AlarmService

class DismissAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DISMISS_ALARM_RECEIVER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "DismissAlarmReceiver triggered")

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "No user logged in, cannot dismiss alarms")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val geofencingClient = LocationServices.getGeofencingClient(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        firestore.collection("alarms")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Log.d(TAG, "No active alarms found for user: $userId")
                } else {
                    val alarmIds = mutableListOf<String>()
                    for (document in querySnapshot.documents) {
                        val alarmId = document.id
                        alarmIds.add(alarmId)
                        firestore.collection("alarms")
                            .document(alarmId)
                            .update("isActive", false)
                            .addOnSuccessListener {
                                Log.d(TAG, "Deactivated alarm: $alarmId")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to deactivate alarm $alarmId: ${e.message}")
                            }
                    }

                    if (alarmIds.isNotEmpty()) {
                        geofencingClient.removeGeofences(alarmIds)
                            .addOnSuccessListener {
                                Log.d(TAG, "Removed geofences for alarms: $alarmIds")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to remove geofences: ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to query active alarms: ${e.message}")
            }

        notificationManager.cancelAll()
        Log.d(TAG, "Cleared all notifications")

        context.stopService(Intent(context, LocationTrackingService::class.java))
        Log.d(TAG, "Stopped LocationTrackingService")

        context.stopService(Intent(context, AlarmService::class.java))
        Log.d(TAG, "Stopped AlarmService")
    }
}