package com.victor.loclarm2.presentation.alarm.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.presentation.alarm.viewmodel.AlarmsViewModel
import com.victor.loclarm2.presentation.home.screens.BottomNavigationBar

@Composable
fun AlarmsScreen(
    navController: NavController,
    viewModel: AlarmsViewModel = hiltViewModel()
) {
    val alarms = viewModel.alarms.collectAsState()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(alarms.value) { alarm ->
                AlarmItem(alarm)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun AlarmItem(alarm: Alarm) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = alarm.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Location: (${alarm.latitude}, ${alarm.longitude})")
            Text(text = "Radius: ${alarm.radius} meters")
        }
    }
}