package com.victor.loclarm2.presentation.alarm.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.domain.repository.AuthRepository
import com.victor.loclarm2.domain.usecase.alarm.AlarmsUseCase
import com.victor.loclarm2.geofence.GeofenceHelper
import com.victor.loclarm2.geofence.LocationTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val useCase: AlarmsUseCase,
    private val authRepository: AuthRepository,
    private val geofenceHelper: GeofenceHelper
) : ViewModel() {

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms

    init {
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            useCase.getAlarms(user.id).onSuccess {
                _alarms.value = it
            }
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            useCase.deleteAlarm(alarmId)
            loadAlarms()
        }
    }

    fun updateAlarm(context: Context, alarm: Alarm) {
        viewModelScope.launch {
            useCase.saveAlarm(alarm)

            if (alarm.active) {
                val latLng = com.google.android.gms.maps.model.LatLng(alarm.latitude, alarm.longitude)
                val pendingIntent = geofenceHelper.getPendingIntent()
                geofenceHelper.addGeofence(latLng, alarm.radius, alarm.id, pendingIntent)

                startLocationTrackingForAlarm(context, alarm)
            } else {
                stopLocationTrackingForAlarm(context, alarm.id)
            }

            loadAlarms()
        }
    }

    private fun startLocationTrackingForAlarm(context: Context, alarm: Alarm) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
            putExtra("TARGET_LATITUDE", alarm.latitude)
            putExtra("TARGET_LONGITUDE", alarm.longitude)
            putExtra("TARGET_RADIUS", alarm.radius)
            putExtra("ALARM_NAME", alarm.name)
            putExtra("ALARM_ID", alarm.id)
        }
        context.startForegroundService(intent)
    }

    private fun stopLocationTrackingForAlarm(context: Context, alarmId: String) {
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.stopService(intent)

        geofenceHelper.removeGeofenceById(alarmId)
    }

}
