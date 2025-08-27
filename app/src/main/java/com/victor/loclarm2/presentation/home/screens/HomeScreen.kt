package com.victor.loclarm2.presentation.home.screens

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.victor.loclarm2.R
import com.victor.loclarm2.presentation.home.viewmodel.HomeViewModel
import com.victor.loclarm2.utils.NetworkAwareContent
import com.victor.loclarm2.utils.requestForegroundServiceLocationPermission
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val scope = rememberCoroutineScope()
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    val backgroundLocationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        null
    }

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        null
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(13.0827, 80.2707), 10f)
    }

    LaunchedEffect(
        locationPermissionState.status,
        backgroundLocationPermissionState?.status,
        notificationPermissionState?.status
    ) {
        val isNotificationPermissionGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionState?.status?.isGranted == true
            } else {
                true
            }

        val isBackgroundLocationPermissionGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                backgroundLocationPermissionState?.status?.isGranted == true
            } else {
                true
            }

        if (locationPermissionState.status.isGranted &&
            isBackgroundLocationPermissionGranted &&
            isNotificationPermissionGranted
        ) {
            viewModel.initializeLocation(context)
            viewModel.fetchActiveAlarms(context)
            if (context is Activity) {
                requestForegroundServiceLocationPermission(context)
            }
        } else {
            when {
                !locationPermissionState.status.isGranted -> {
                    locationPermissionState.launchPermissionRequest()
                }

                locationPermissionState.status.isGranted &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        backgroundLocationPermissionState?.status?.isGranted == false -> {
                    backgroundLocationPermissionState.launchPermissionRequest()
                }

                locationPermissionState.status.isGranted &&
                        isBackgroundLocationPermissionGranted &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        notificationPermissionState?.status?.isGranted == false -> {
                    notificationPermissionState.launchPermissionRequest()
                }
            }
        }
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(loc, 14f)
        }
    }

    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_dark_style)
        )
    }

    NetworkAwareContent {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    if (latLng.latitude in -90.0..90.0 && latLng.longitude in -180.0..180.0) {
                        viewModel.setSelectedLocation(latLng.latitude, latLng.longitude)
                        viewModel.setShowBottomSheet(true)
                    }
                },
                uiSettings = MapUiSettings(zoomControlsEnabled = false, compassEnabled = false),
                properties = mapProperties
            ) {
                val activeAlarms by viewModel.activeAlarms.collectAsState()
                activeAlarms.forEach { alarm ->
                    val center = LatLng(alarm.latitude, alarm.longitude)
                    Marker(
                        state = MarkerState(position = center),
                        title = alarm.name
                    )
                    Circle(
                        center = center,
                        radius = alarm.radius.toDouble(),
                        strokeColor = Color.Red,
                        fillColor = Color.Red.copy(alpha = 0.2f),
                        strokeWidth = 2f
                    )
                }
            }
            SearchAndLocationBar(viewModel, cameraPositionState, context)

            if (showBottomSheet) {
                SetAlarmBottomSheet(
                    viewModel,
                    onSave = { name, radius, isActive ->
                        scope.launch {
                            viewModel.saveAlarm(context, name, radius, isActive)
                            viewModel.setShowBottomSheet(false, isCancelled = false)
                        }
                    },
                    onDiscard = {
                        viewModel.setShowBottomSheet(false, isCancelled = true)
                    }
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                BottomNavigationBar(navController)
            }
        }
    }
}