package com.victor.loclarm2.presentation.home.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.victor.loclarm2.presentation.home.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
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
    val selectedLocation = viewModel.selectedLocation.collectAsState()
    val showBottomSheet = viewModel.showBottomSheet.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(viewModel)
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    viewModel.setSelectedLocation(latLng.latitude, latLng.longitude)
                    viewModel.setShowBottomSheet(true)
                }
            ) {
                selectedLocation.value?.let { loc ->
                    Marker(
                        state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                        title = "Selected Location"
                    )
                }
            }

            if (showBottomSheet.value) {
                AlarmBottomSheet(
                    onSave = { name, radius ->
                        scope.launch {
                            viewModel.saveAlarm(name, radius)
                            viewModel.setShowBottomSheet(false)
                        }
                    },
                    onDiscard = {
                        viewModel.setShowBottomSheet(false)
                    }
                )
            }
        }
    }
}

@Composable
fun SearchBar(viewModel: HomeViewModel) {
    var query by remember { mutableStateOf("") }
    OutlinedTextField(
        value = query,
        onValueChange = { query = it },
        label = { Text("Search Location") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
    // Implement search functionality using Google Places API if needed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBottomSheet(
    onSave: (String, Float) -> Unit,
    onDiscard: () -> Unit
) {
    var alarmName by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("100") }

    ModalBottomSheet(
        onDismissRequest = { onDiscard() }
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
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = radius,
                onValueChange = { radius = it },
                label = { Text("Radius (meters)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { onDiscard() }) {
                    Text("Discard")
                }
                Button(onClick = {
                    onSave(alarmName, radius.toFloatOrNull() ?: 100f)
                }) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = navController.currentDestination?.route == "home",
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = navController.currentDestination?.route == "alarms",
            onClick = { navController.navigate("alarms") },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Alarms") },
            label = { Text("Alarms") }
        )
        NavigationBarItem(
            selected = navController.currentDestination?.route == "settings",
            onClick = { navController.navigate("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}