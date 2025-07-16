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
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.domain.model.Location
import com.victor.loclarm2.domain.repository.AuthRepository
import com.victor.loclarm2.domain.usecase.alarm.SaveAlarmUseCase
import com.victor.loclarm2.data.geofence.AlarmServiceScheduler
import com.victor.loclarm2.data.geofence.GeofenceHelper
import com.victor.loclarm2.data.geofence.LocationForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val saveAlarmUseCase: SaveAlarmUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedLocation = MutableStateFlow<Location?>(null)
    val selectedLocation: StateFlow<Location?> = _selectedLocation

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet

    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults

    private val _selectedRadius = MutableStateFlow(1000f)
    val selectedRadius: StateFlow<Float> = _selectedRadius
    fun setSelectedRadius(radius: Float) {
        _selectedRadius.value = radius
    }
    @Inject lateinit var alarmServiceScheduler: AlarmServiceScheduler
    @Inject lateinit var geofenceHelper: GeofenceHelper

    private val _selectedAlarmActive = MutableStateFlow(false)
    val selectedAlarmActive: StateFlow<Boolean> = _selectedAlarmActive
    fun setSelectedAlarmActive(active: Boolean) {
        _selectedAlarmActive.value = active
    }


    fun initializeLocation(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        updateCurrentLocation(context)
    }

    fun updateCurrentLocation(context: Context, cameraPositionState: CameraPositionState? = null) {
        viewModelScope.launch {
            val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                try {
                    val locationResult = fusedLocationClient.lastLocation.await()
                    locationResult?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        _currentLocation.value = latLng
                        cameraPositionState?.position = CameraPosition.fromLatLngZoom(latLng, 10f)
                    }
                } catch (e: SecurityException) {
                    // Handle permission denied (e.g., show dialog via UI)
                } catch (e: Exception) {
                    // Handle other exceptions
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
                    val predictions = response.autocompletePredictions.map { it.getFullText(null).toString() }
                    _searchResults.value = predictions
                } catch (e: Exception) {
                    _searchResults.value = emptyList()
                }
            } else {
                _searchResults.value = emptyList()
            }
        }
    }

    suspend fun getLatLngFromPlace(placeName: String, context: Context): LatLng {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocationName(placeName, 1)
        return if (addresses != null && addresses.isNotEmpty()) {
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

    fun saveAlarm(context: Context, name: String, radius: Float, isActive: Boolean) {
        val serviceIntent = Intent(context, LocationForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

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
                isActive = isActive
            )
            saveAlarmUseCase(alarm)
            if (isActive) {
                alarmServiceScheduler.scheduleLocationWorker(alarm)
                val latLng = LatLng(location.latitude, location.longitude)
                val pendingIntent = geofenceHelper.getPendingIntent()
                geofenceHelper.addGeofence(latLng, radius, alarm.id, pendingIntent)
                val serviceIntent = Intent(context, LocationForegroundService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
            }
        }
    }
}