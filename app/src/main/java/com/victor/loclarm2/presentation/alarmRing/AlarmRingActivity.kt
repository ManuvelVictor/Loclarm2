package com.victor.loclarm2.presentation.alarmRing

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.victor.loclarm2.ui.theme.Loclarm2Theme

class AlarmRingActivity : ComponentActivity() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val alarmName = intent.getStringExtra("alarmName") ?: "Alarm"

        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        ringtone = RingtoneManager.getRingtone(this, alarmUri)
        ringtone?.play()

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0)
        vibrator?.vibrate(effect)

        setContent {
            Loclarm2Theme {
                AlarmRingScreen(alarmName = alarmName) {
                    stopAlarm()
                    finish()
                }
            }
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
}