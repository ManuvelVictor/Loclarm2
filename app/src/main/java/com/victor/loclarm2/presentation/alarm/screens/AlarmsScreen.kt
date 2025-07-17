package com.victor.loclarm2.presentation.alarm.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.presentation.alarm.viewmodel.AlarmsViewModel
import com.victor.loclarm2.presentation.home.screens.BottomNavigationBar
import com.victor.loclarm2.utils.GlassAlarmItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    navController: NavController,
    viewModel: AlarmsViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedAlarm by remember { mutableStateOf<Alarm?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarms") }
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(alarms) { alarm ->
                    GlassAlarmItem(
                        alarm = alarm,
                        onEdit = {
                            selectedAlarm = alarm
                            showBottomSheet = true
                        },
                        onDelete = { viewModel.deleteAlarm(alarm.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (showBottomSheet && selectedAlarm != null) {
                EditAlarmBottomSheet(
                    alarm = selectedAlarm!!,
                    onDismiss = {
                        showBottomSheet = false
                        selectedAlarm = null
                    },
                    onSave = { updatedAlarm ->
                        viewModel.updateAlarm(updatedAlarm)
                        showBottomSheet = false
                        selectedAlarm = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmBottomSheet(
    alarm: Alarm,
    onDismiss: () -> Unit,
    onSave: (Alarm) -> Unit
) {
    var alarmName by remember { mutableStateOf(alarm.name) }
    var radius by remember { mutableFloatStateOf(alarm.radius / 1000f) }
    var isActive by remember { mutableStateOf(alarm.isActive) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                onValueChange = { radius = it },
                valueRange = 1f..50f,
                steps = 49,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Active")
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onDismiss) { Text("Cancel") }
                Button(onClick = {
                    val updated = alarm.copy(
                        name = alarmName,
                        radius = radius * 1000f,
                        isActive = isActive
                    )
                    onSave(updated)
                }) {
                    Text("Save")
                }
            }
        }
    }
}
