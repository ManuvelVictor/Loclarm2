package com.victor.loclarm2.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DismissAlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DISMISS_ALARM_RECEIVER"
    }

    override fun onReceive(context: Context, intent: Intent) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
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
                            }
                    }

                    if (alarmIds.isNotEmpty()) {
                        geofencingClient.removeGeofences(alarmIds)
                    }
                }
            }

        notificationManager.cancelAll()

        context.stopService(Intent(context, LocationTrackingService::class.java))
    }
}