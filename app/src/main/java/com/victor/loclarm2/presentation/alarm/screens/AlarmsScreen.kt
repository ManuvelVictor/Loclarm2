package com.victor.loclarm2.presentation.alarm.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.presentation.alarm.viewmodel.AlarmsViewModel
import com.victor.loclarm2.presentation.home.screens.BottomNavigationBar
import com.victor.loclarm2.presentation.home.viewmodel.HomeViewModel
import com.victor.loclarm2.utils.NetworkAwareContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    navController: NavController,
    viewModel: AlarmsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedAlarm by remember { mutableStateOf<Alarm?>(null) }
    val localContext: Context = LocalContext.current

    NetworkAwareContent {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Alarms",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (alarms.isNotEmpty()) {
                                Text(
                                    "${alarms.size} active ${if (alarms.size == 1) "alarm" else "alarms"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = { BottomNavigationBar(navController, homeViewModel) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (alarms.isEmpty()) {
                    EmptyStateView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = alarms,
                            key = { it.id }
                        ) { alarm ->
                            SwipeToDeleteAlarmCard(
                                alarm = alarm,
                                onEdit = {
                                    selectedAlarm = alarm
                                    showBottomSheet = true
                                },
                                onDelete = {
                                    viewModel.deleteAlarm(alarm.id)
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
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
                            viewModel.updateAlarm(localContext, updatedAlarm)
                            showBottomSheet = false
                            selectedAlarm = null
                        }
                    )
                }
            }
        }
    }
}