package com.victor.loclarm2.geofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.victor.loclarm2.R
import com.victor.loclarm2.presentation.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    companion object {
        const val ALARM_CHANNEL_ID = "AlarmChannel"
        const val ALARM_NOTIFICATION_ID = 2
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        createAlarmNotificationChannel()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getStringExtra("ALARM_ID") ?: ""
        Log.d("ALARM_SERVICE", "Alarm triggered for ID: $alarmId")

        startForeground(ALARM_NOTIFICATION_ID, createAlarmNotification(alarmId))
        startAlarmSound()
        startVibration()

        serviceScope.launch {
            delay(60000)
            stopAlarm()
        }

        return START_NOT_STICKY
    }

    private fun createAlarmNotification(alarmId: String): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val dismissIntent = Intent(this, DismissAlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
            putExtra("ALARM_ID", alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setContentTitle("🚨 Location Alarm!")
            .setContentText("You've reached your destination!")
            .setSmallIcon(R.drawable.map_pin)
            .setContentIntent(mainPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(R.drawable.close_icon, "Dismiss", dismissPendingIntent)
            .setFullScreenIntent(mainPendingIntent, true)
            .build()
    }

    private fun startAlarmSound() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_ALARM)
                setDataSource(
                    this@LocationTrackingService,
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("ALARM_SERVICE", "Error starting alarm sound: ${e.message}")
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioStreamType(AudioManager.STREAM_NOTIFICATION)
                    setDataSource(
                        this@LocationTrackingService,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (fallbackException: Exception) {
                Log.e(
                    "ALARM_SERVICE",
                    "Error starting fallback sound: ${fallbackException.message}"
                )
            }
        }
    }

    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } catch (e: Exception) {
            Log.e("ALARM_SERVICE", "Error starting vibration: ${e.message}")
        }
    }

    fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createAlarmNotificationChannel() {
        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Location Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for location-based alarm notifications"
            enableVibration(true)
            setShowBadge(true)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}