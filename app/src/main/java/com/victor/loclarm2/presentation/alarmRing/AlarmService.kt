package com.victor.loclarm2.presentation.alarmRing

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.victor.loclarm2.R
import com.victor.loclarm2.geofence.DismissAlarmReceiver
import com.victor.loclarm2.presentation.MainActivity

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "AlarmChannel"
    }

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getStringExtra("alarmId") ?: return START_NOT_STICKY
        Log.d("ALARM_SERVICE", "Starting AlarmService for alarmId: $alarmId")

        startForeground(2, buildNotification(alarmId))

        audioManager.requestAudioFocus(
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .build()
        )

        mediaPlayer = MediaPlayer.create(this, R.raw.retro_game)
        if (mediaPlayer == null) {
            Log.e("ALARM", "Failed to create MediaPlayer for retro_game")
            stopSelf()
            return START_NOT_STICKY
        }
        mediaPlayer?.apply {
            isLooping = true
            start()
        }

        return START_STICKY
    }

    private fun buildNotification(alarmId: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val dismissIntent = Intent(this, DismissAlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        createNotificationChannel()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Triggered")
            .setContentText("You have entered the alarm radius for ID: $alarmId")
            .setSmallIcon(R.drawable.map_pin)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.close_icon, "Dismiss", dismissPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarm Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for alarm notifications"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        audioManager.abandonAudioFocusRequest(
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).build()
        )
        Log.d("ALARM_SERVICE", "AlarmService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}