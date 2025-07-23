package com.victor.loclarm2.geofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.victor.loclarm2.R
import com.victor.loclarm2.presentation.MainActivity

class LocationTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "LoclarmTrackingChannel"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("LOCATION_SERVICE", "LocationTrackingService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LOCATION_SERVICE", "LocationTrackingService started")
        startForeground(1, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val dismissIntent = Intent(this, DismissAlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Loclarm is tracking you")
            .setContentText("Monitoring active alarms")
            .setSmallIcon(R.drawable.map_pin)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.close_icon, "Dismiss", dismissPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        Log.d("LOCATION_SERVICE", "Notification created with ID: 1")
        return notification
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Loclarm Tracking Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for location tracking notifications"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LOCATION_SERVICE", "LocationTrackingService destroyed")
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}