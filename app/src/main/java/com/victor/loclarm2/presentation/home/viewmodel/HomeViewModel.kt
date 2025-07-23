package com.victor.loclarm2.presentation.home.viewmodel

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.util.Log
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _selectedRadius = MutableStateFlow(1000f)
    val selectedRadius: StateFlow<Float> = _selectedRadius

    private val _selectedAlarmActive = MutableStateFlow(false)
    val selectedAlarmActive: StateFlow<Boolean> = _selectedAlarmActive

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val _activeAlarms = MutableStateFlow<List<Alarm>>(emptyList())
    val activeAlarms: StateFlow<List<Alarm>> = _activeAlarms


    fun initializeLocation(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        Log.d("PERMISSIONS", "Fine Location: ${ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED}")
        Log.d("PERMISSIONS", "Background Location: ${ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED}")
        updateCurrentLocation(context)
    }

    fun updateCurrentLocation(context: Context, cameraPositionState: CameraPositionState? = null) {
        viewModelScope.launch {
            val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(context, permission) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    val locationResult = fusedLocationClient.lastLocation.await()
                    locationResult?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        _currentLocation.value = latLng
                        cameraPositionState?.position = CameraPosition.fromLatLngZoom(latLng, 10f)
                    }
                } catch (_: Exception) {
                }
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
                }

                if (activeAlarms.isNotEmpty()) {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, LocationTrackingService::class.java)
                    )
                }
            }
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
                    val predictions =
                        response.autocompletePredictions.map { it.getFullText(null).toString() }
                    _searchResults.value = predictions
                } catch (_: Exception) {
                    _searchResults.value = emptyList()
                }
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    fun getLatLngFromPlace(placeName: String, context: Context): LatLng {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocationName(placeName, 1)
        return if (!addresses.isNullOrEmpty()) {
            LatLng(addresses[0].latitude, addresses[0].longitude)
        } else {
            LatLng(0.0, 0.0)
        }
    }

    fun setSelectedLocation(latitude: Double, longitude: Double) {
        _selectedLocation.value = Location(latitude, longitude)
        _selectedAlarmActive.value = false
    }

    fun setShowBottomSheet(show: Boolean, isCancelled: Boolean = false) {
        _showBottomSheet.value = show
        if (!show && isCancelled) {
            _selectedAlarmActive.value = false
        }
    }

    fun setSelectedRadius(radius: Float) {
        _selectedRadius.value = radius
    }

    fun setSelectedAlarmActive(active: Boolean) {
        _selectedAlarmActive.value = active
    }

    fun saveAlarm(context: Context, name: String, radius: Float, isActive: Boolean) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            val location = _selectedLocation.value ?: return@launch
            Log.d("ALARM_SAVE", "Saving alarm: $name, Active: $isActive, Radius: $radius, Lat: ${location.latitude}, Lng: ${location.longitude}")

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
            Log.d("ALARM_SAVE", "Saved alarm: $name, Active: $isActive, Radius: $radius")

            if (isActive) {
                val serviceIntent = Intent(context, LocationTrackingService::class.java).apply {
                    putExtra("destinationLat", location.latitude)
                    putExtra("destinationLng", location.longitude)
                    putExtra("radius", radius.toInt())
                }
                ContextCompat.startForegroundService(context, serviceIntent)
                Log.d("ALARM_SAVE", "Started LocationTrackingService for alarm: ${alarm.id}")

                val latLng = LatLng(location.latitude, location.longitude)
                val pendingIntent = geofenceHelper.getPendingIntent()
                geofenceHelper.addGeofence(latLng, radius, alarm.id, pendingIntent)
                Log.d("ALARM_SAVE", "Added geofence for alarm: ${alarm.id}")
            }
        }
    }
}
