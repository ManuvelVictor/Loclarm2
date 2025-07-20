package com.victor.loclarm2.data.geofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.IBinder
import com.victor.loclarm2.R
import com.victor.loclarm2.presentation.MainActivity
import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "LoclarmTrackingChannel"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var destinationLat = 0.0
    private var destinationLng = 0.0
    private var radius = 100
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var entered = false
    private lateinit var audioManager: AudioManager

    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = getSystemService(Vibrator::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    checkProximity(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        destinationLat = intent?.getDoubleExtra("destinationLat", 0.0) ?: 0.0
        destinationLng = intent?.getDoubleExtra("destinationLng", 0.0) ?: 0.0
        radius = intent?.getIntExtra("radius", 100) ?: 100

        startForeground(1, buildNotification())
        startLocationUpdates()
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val dismissIntent =
            Intent(this, DismissAlarmReceiver::class.java).apply { action = "DISMISS_ALARM" }
        val dismissPendingIntent =
            PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_IMMUTABLE)

        createNotificationChannel()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Loclarm is tracking you")
            .setContentText("You will be alerted upon entering the destination radius")
            .setSmallIcon(R.drawable.map_pin)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.close_icon, "Dismiss", dismissPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Loclarm Tracking Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun startLocationUpdates() {
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000).build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun checkProximity(location: Location) {
        val results = FloatArray(1)
        Location.distanceBetween(
            location.latitude,
            location.longitude,
            destinationLat,
            destinationLng,
            results
        )

        val distance = results[0]
        if (distance <= radius && !entered) {
            entered = true
            triggerAlarm()
        } else if (distance > radius && entered) {
            entered = false
            stopAlarm()
        }
    }

    private fun triggerAlarm() {
        mediaPlayer = MediaPlayer.create(this, R.raw.retro_game).apply {
            isLooping = true
            start()
        }
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
    }

    private fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
