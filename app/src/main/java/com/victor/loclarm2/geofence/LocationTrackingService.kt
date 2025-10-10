package com.victor.loclarm2.geofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.victor.loclarm2.R
import com.victor.loclarm2.data.local.SettingsDataStore
import com.victor.loclarm2.presentation.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class LocationTrackingService : Service() {

    companion object {
        const val TRACKING_CHANNEL_ID = "TrackingChannel"
        const val ALARM_CHANNEL_ID = "AlarmChannel"
        const val TRACKING_NOTIFICATION_ID = 1
        const val ALARM_NOTIFICATION_ID = 2

        const val ACTION_START_TRACKING = "START_TRACKING"
        const val ACTION_STOP_TRACKING = "STOP_TRACKING"
        const val ACTION_TRIGGER_ALARM = "TRIGGER_ALARM"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private lateinit var dataStore: SettingsDataStore

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var targetLatitude = 0.0
    private var targetLongitude = 0.0
    private var targetRadius = 0f
    private var alarmName = ""
    private var alarmId = ""
    private var isTracking = false
    private var isAlarmTriggered = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        vibrator = getSystemService(Vibrator::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        dataStore = SettingsDataStore(applicationContext)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (!isTracking || isAlarmTriggered) return
                locationResult.lastLocation?.let { checkDistanceToTarget(it) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                targetLatitude = intent.getDoubleExtra("TARGET_LATITUDE", 0.0)
                targetLongitude = intent.getDoubleExtra("TARGET_LONGITUDE", 0.0)
                targetRadius = intent.getFloatExtra("TARGET_RADIUS", 0f)
                alarmName = intent.getStringExtra("ALARM_NAME") ?: ""
                alarmId = intent.getStringExtra("ALARM_ID") ?: ""
                startLocationTracking()
            }
            ACTION_STOP_TRACKING -> stopLocationTracking()
            ACTION_TRIGGER_ALARM -> triggerAlarm()
            else -> {
                val legacyAlarmId = intent?.getStringExtra("ALARM_ID") ?: ""
                if (legacyAlarmId.isNotEmpty()) triggerAlarm()
            }
        }
        return START_NOT_STICKY
    }

    private fun startLocationTracking() {
        if (isTracking) return

        try {
            isTracking = true
            isAlarmTriggered = false

            startForeground(TRACKING_NOTIFICATION_ID, createTrackingNotification())

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            stopLocationTracking()
        }
    }

    private fun stopLocationTracking() {
        if (!isTracking) return

        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(TRACKING_NOTIFICATION_ID)

        if (!isAlarmTriggered) stopSelf()
    }

    private fun checkDistanceToTarget(currentLocation: Location) {
        val targetLocation = Location("target").apply {
            latitude = targetLatitude
            longitude = targetLongitude
        }

        val distance = currentLocation.distanceTo(targetLocation)
        if (distance <= targetRadius) triggerAlarm()
    }

    private fun triggerAlarm() {
        if (isAlarmTriggered) return
        isAlarmTriggered = true

        stopLocationTracking()
        startForeground(ALARM_NOTIFICATION_ID, createAlarmNotification())

        serviceScope.launch {
            val volume = dataStore.volume.first()
            val ringtoneKey = dataStore.ringtone.first()
            val vibrationEnabled = dataStore.vibration.first()

            val resId = when (ringtoneKey) {
                "default_1" -> R.raw.default_alarm_1
                "default_2" -> R.raw.default_alarm_2
                "default_3" -> R.raw.default_alarm_3
                "default_4" -> R.raw.default_alarm_4
                else -> R.raw.default_alarm_1
            }

            withContext(Dispatchers.Main) {
                playSound(resId, volume)
                if (vibrationEnabled) startVibration()
            }
        }
    }

    private fun playSound(resId: Int, volume: Float) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, resId).apply {
                setVolume(volume, volume)
                isLooping = true
                start()
            }
        } catch (_: Exception) {
            // fallback to default system sound
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(
                        this@LocationTrackingService,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } catch (_: Exception) {
        }
    }

    fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(ALARM_NOTIFICATION_ID)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createTrackingNotification(): Notification {
        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = ACTION_STOP_TRACKING
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, TRACKING_CHANNEL_ID)
            .setContentTitle("🎯 Tracking Location")
            .setContentText("Monitoring your location for alarm: $alarmName")
            .setSmallIcon(R.drawable.map_pin)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .addAction(R.drawable.close_icon, "Stop Tracking", stopPendingIntent)
            .build()
    }

    private fun createAlarmNotification(): Notification {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("SHOW_ALARM_DIALOG", true)
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_NAME", alarmName)
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val dismissIntent = Intent(this, DismissAlarmReceiver::class.java).apply {
            action = "DISMISS_ALARM"
            putExtra("ALARM_ID", alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this, 0, dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setContentTitle("🚨 Location Alarm!")
            .setContentText("You've reached your destination: $alarmName")
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

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val trackingChannel = NotificationChannel(
            TRACKING_CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Ongoing location tracking notifications"
            enableVibration(false)
            setShowBadge(false)
        }

        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Location Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for location-based alarm notifications"
            enableVibration(true)
            setShowBadge(true)
        }

        manager.createNotificationChannel(trackingChannel)
        manager.createNotificationChannel(alarmChannel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        stopLocationTracking()
        stopAlarm()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}