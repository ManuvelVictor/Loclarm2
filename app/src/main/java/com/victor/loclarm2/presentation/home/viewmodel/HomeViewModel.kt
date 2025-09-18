package com.victor.loclarm2.presentation.home.viewmodel

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.CameraPositionState
import com.victor.loclarm2.geofence.GeofenceHelper
import com.victor.loclarm2.geofence.LocationTrackingService
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.domain.model.Location
import com.victor.loclarm2.domain.repository.AuthRepository
import com.victor.loclarm2.domain.usecase.alarm.AlarmsUseCase
import com.victor.loclarm2.presentation.home.screens.getFromLocationNameAsync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val alarmUseCase: AlarmsUseCase,
    private val authRepository: AuthRepository,
    private val geofenceHelper: GeofenceHelper,
) : ViewModel() {

    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet

    private val _activeAlarms = MutableStateFlow<List<Alarm>>(emptyList())
    val activeAlarms: StateFlow<List<Alarm>> = _activeAlarms

    private val _showAlarmDialog = MutableStateFlow(false)
    val showAlarmDialog: StateFlow<Boolean> = _showAlarmDialog

    private val _currentTriggeredAlarm = MutableStateFlow<Alarm?>(null)
    val currentTriggeredAlarm: StateFlow<Alarm?> = _currentTriggeredAlarm

    private var fusedLocationClient: FusedLocationProviderClient? = null

    fun initializeLocation(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        updateCurrentLocation(context)
    }

    fun updateCurrentLocation(context: Context, cameraPositionState: CameraPositionState? = null) {
        viewModelScope.launch {
            val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(context, permission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    val locationResult = fusedLocationClient?.lastLocation?.await()
                    locationResult?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        _currentLocation.value = latLng
                        cameraPositionState?.position = CameraPosition.fromLatLngZoom(latLng, 10f)
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun fetchActiveAlarms(context: Context) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            alarmUseCase.getAlarms(user.id).onSuccess { alarms ->
                val activeAlarms = alarms.filter { it.active }
                _activeAlarms.value = activeAlarms

                val pendingIntent = geofenceHelper.getPendingIntent()
                activeAlarms.forEach { alarm ->
                    val latLng = LatLng(alarm.latitude, alarm.longitude)
                    geofenceHelper.addGeofence(latLng, alarm.radius, alarm.id, pendingIntent)
                    startLocationTrackingForAlarm(context, alarm)
                }
            }
        }
    }

    fun startLocationTrackingForAlarm(context: Context, alarm: Alarm) {
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

    fun stopLocationTrackingForAlarm(context: Context, alarmId: String) {
        viewModelScope.launch {
            alarmUseCase.getAlarms(authRepository.getCurrentUser()?.id ?: "")
                .onSuccess { alarms ->
                    val alarm = alarms.find { it.id == alarmId }
                    alarm?.let {
                        val updatedAlarm = it.copy(active = false)
                        alarmUseCase.saveAlarm(updatedAlarm)

                        val updatedActiveAlarms = _activeAlarms.value.filter { activeAlarm ->
                            activeAlarm.id != alarmId
                        }
                        _activeAlarms.value = updatedActiveAlarms
                    }
                }

            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = LocationTrackingService.ACTION_STOP_TRACKING
            }
            context.stopService(intent)
        }
    }

    fun showAlarmDialog(alarm: Alarm) {
        _currentTriggeredAlarm.value = alarm
        _showAlarmDialog.value = true
    }

    fun hideAlarmDialog() {
        _showAlarmDialog.value = false
        _currentTriggeredAlarm.value = null
    }

    fun stopTriggeredAlarm(context: Context) {
        _currentTriggeredAlarm.value?.let { alarm ->
            stopLocationTrackingForAlarm(context, alarm.id)

            val intent = Intent(context, LocationTrackingService::class.java)
            context.stopService(intent)

            hideAlarmDialog()
        }
    }

    fun searchLocation(query: String, context: Context) {
        viewModelScope.launch {
            if (query.isNotEmpty()) {
                val placesClient: PlacesClient = Places.createClient(context)
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(query)
                    .build()
                try {
                    val response = placesClient.findAutocompletePredictions(request).await()
                    _searchResults.value =
                        response.autocompletePredictions.map { it.getFullText(null).toString() }
                } catch (_: Exception) {
                    _searchResults.value = emptyList()
                }
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    suspend fun getLatLngFromPlace(placeName: String, context: Context): LatLng {
        return try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocationNameAsync(placeName, 1)
            if (addresses.isNotEmpty()) {
                LatLng(addresses[0].latitude, addresses[0].longitude)
            } else LatLng(0.0, 0.0)
        } catch (_: Exception) {
            LatLng(0.0, 0.0)
        }
    }

    fun setSelectedLocation(latitude: Double, longitude: Double) {
        _selectedLocation.value = Location(latitude, longitude)
    }

    fun setShowBottomSheet(show: Boolean) {
        _showBottomSheet.value = show
    }

    fun saveAlarm(context: Context, name: String, radius: Float, isActive: Boolean) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            val location = _selectedLocation.value ?: return@launch
            val alarm = Alarm(
                id = UUID.randomUUID().toString(),
                name = name,
                latitude = location.latitude,
                longitude = location.longitude,
                radius = radius,
                userId = user.id,
                active = isActive
            )

            alarmUseCase.saveAlarm(alarm)

            if (isActive) {
                val latLng = LatLng(location.latitude, location.longitude)
                val pendingIntent = geofenceHelper.getPendingIntent()
                geofenceHelper.addGeofence(latLng, radius, alarm.id, pendingIntent)

                startLocationTrackingForAlarm(context, alarm)

                val updatedAlarms = _activeAlarms.value.toMutableList()
                updatedAlarms.add(alarm)
                _activeAlarms.value = updatedAlarms
            }
        }
    }
}