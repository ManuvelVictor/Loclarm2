package com.victor.loclarm2.presentation.home.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.victor.loclarm2.R
import com.victor.loclarm2.presentation.home.viewmodel.HomeViewModel
import com.victor.loclarm2.utils.GlassBox
import com.victor.loclarm2.utils.requestForegroundServiceLocationPermission
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
    }

    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
        viewModel.initializeLocation(context)

        if (context is Activity) {
            requestForegroundServiceLocationPermission(context)
        }

        viewModel.fetchActiveAlarms()

    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(loc, 14f)
        }
    }

    val mapProperties = remember() {
        MapProperties(
            mapStyleOptions =
                MapStyleOptions.loadRawResourceStyle(context, R.raw.map_dark_style)

        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLongClick = { latLng ->
                viewModel.setSelectedLocation(latLng.latitude, latLng.longitude)
                viewModel.setShowBottomSheet(true)
            },
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false
            ),
            properties = mapProperties
        ) {
            selectedLocation?.let { loc ->
                val center = LatLng(loc.latitude, loc.longitude)
                Marker(state = MarkerState(position = center), title = "Selected Location")

                val isActive = viewModel.selectedAlarmActive.collectAsState().value
                if (isActive) {
                    Circle(
                        center = center,
                        radius = viewModel.selectedRadius.collectAsState().value.toDouble(),
                        strokeColor = Color.Blue,
                        fillColor = Color.Blue.copy(alpha = 0.2f),
                        strokeWidth = 2f
                    )
                }
            }

            val activeAlarms by viewModel.activeAlarms.collectAsState()

            activeAlarms.forEach { alarm ->
                val center = LatLng(alarm.latitude, alarm.longitude)
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
                onDiscard = { viewModel.setShowBottomSheet(false, isCancelled = true)
                },
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

@Composable
fun SearchAndLocationBar(
    viewModel: HomeViewModel,
    cameraPositionState: CameraPositionState,
    context: Context
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    val viewModelSearchResults = viewModel.searchResults.collectAsState()
    var searchResults by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(viewModelSearchResults.value) {
        searchResults = viewModelSearchResults.value
    }
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 60.dp)
            .fillMaxWidth()
    ) {
        GlassBox(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.searchLocation(it, context)
                    },
                    placeholder = { Text("Alarm location?..") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.primary,
                        focusedPlaceholderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = {
                    viewModel.updateCurrentLocation(context, cameraPositionState)
                    searchResults = emptyList()
                }) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "My Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (searchResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            GlassBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    searchResults.forEach { placeName ->
                        OutlinedButton(
                            onClick = {
                                query = placeName
                                viewModel.searchLocation(placeName, context)
                                scope.launch {
                                    val latLng = viewModel.getLatLngFromPlace(placeName, context)
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                    searchResults = emptyList()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(placeName, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAlarmBottomSheet(
    viewModel: HomeViewModel,
    onSave: (String, Float, Boolean) -> Unit,
    onDiscard: () -> Unit
) {
    var alarmName by remember { mutableStateOf("") }
    var radius by remember { mutableFloatStateOf(1f) }
    var isActive by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { onDiscard() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = alarmName,
                onValueChange = { alarmName = it },
                label = { Text("Alarm Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Radius (km): ${radius.toInt()}")
            Slider(
                value = radius,
                onValueChange = {
                    radius = it
                    viewModel.setSelectedRadius(it * 1000f)
                },
                valueRange = 1f..50f,
                steps = 49,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Activate")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isActive,
                    onCheckedChange = {
                        isActive = it
                        viewModel.setSelectedAlarmActive(it)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { onDiscard() }) {
                    Text("Discard")
                }
                Button(onClick = {
                    onSave(alarmName, radius * 1000f, isActive)
                }) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = Color.White
    GlassBox(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBarItem(
                selected = navController.currentDestination?.route == "home",
                onClick = { navController.navigate("home") },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    indicatorColor = selectedColor.copy(alpha = 0.12f),
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor
                )
            )
            NavigationBarItem(
                selected = navController.currentDestination?.route == "alarms",
                onClick = { navController.navigate("alarms") },
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Alarms") },
                label = { Text("Alarms") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    indicatorColor = selectedColor.copy(alpha = 0.12f),
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor
                )
            )
            NavigationBarItem(
                selected = navController.currentDestination?.route == "settings",
                onClick = { navController.navigate("settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    indicatorColor = selectedColor.copy(alpha = 0.12f),
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor
                )
            )
        }
    }
}
