package com.victor.loclarm2.presentation.home.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraPositionState
import com.victor.loclarm2.presentation.home.viewmodel.HomeViewModel
import com.victor.loclarm2.utils.GlassBox
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAlarmBottomSheet(
    viewModel: HomeViewModel,
    onSave: (String, Float, Boolean) -> Unit,
    onDiscard: () -> Unit
) {
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    var alarmName by remember { mutableStateOf("") }
    var selectedRadius by remember { mutableFloatStateOf(1000f) }
    var isAlarmActive by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDiscard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set Location Alarm",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            selectedLocation?.let { location ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Selected Location:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            OutlinedTextField(
                value = alarmName,
                onValueChange = { alarmName = it },
                label = { Text("Alarm Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true
            )

            Text(
                text = "Radius: ${selectedRadius.toInt()}m",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Slider(
                value = selectedRadius,
                onValueChange = { selectedRadius = it },
                valueRange = 100f..5000f,
                steps = 49,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activate Alarm",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = isAlarmActive,
                    onCheckedChange = { isAlarmActive = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDiscard,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (alarmName.isNotBlank()) {
                            onSave(alarmName, selectedRadius, isAlarmActive)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = alarmName.isNotBlank()
                ) {
                    Text("Save Alarm")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val isDarkTheme = isSystemInDarkTheme()
    val selectedColor = MaterialTheme.colorScheme.primary

    val unselectedColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    val indicatorColor = if (isDarkTheme) {
        selectedColor.copy(alpha = 0.2f)
    } else {
        selectedColor.copy(alpha = 0.15f)
    }

    GlassBox(
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBarItem(
                selected = navController.currentDestination?.route == "home",
                onClick = { navController.navigate("home") },
                icon = {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home"
                    )
                },
                label = { Text("Home") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    indicatorColor = indicatorColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor
                )
            )
            NavigationBarItem(
                selected = navController.currentDestination?.route == "alarms",
                onClick = { navController.navigate("alarms") },
                icon = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Alarms"
                    )
                },
                label = { Text("Alarms") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    indicatorColor = indicatorColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor
                )
            )
            NavigationBarItem(
                selected = navController.currentDestination?.route == "settings",
                onClick = { navController.navigate("settings") },
                icon = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                },
                label = { Text("Settings") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    indicatorColor = indicatorColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor
                )
            )
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
                        focusedBorderColor = Color.Transparent
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
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            latLng,
                                            15f
                                        )
                                    )
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

@Composable
fun AlarmTriggeredDialog(
    alarmName: String,
    onStopAlarm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🚨 Location Alarm Triggered!")
        },
        text = {
            Text("You have reached your destination: $alarmName\n\nWould you like to stop this alarm?")
        },
        confirmButton = {
            TextButton(
                onClick = onStopAlarm
            ) {
                Text("Stop Alarm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Keep Alarm")
            }
        }
    )
}