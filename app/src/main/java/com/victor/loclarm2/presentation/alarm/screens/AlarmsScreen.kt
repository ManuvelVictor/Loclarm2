package com.victor.loclarm2.presentation.alarm.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.R
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.presentation.alarm.viewmodel.AlarmsViewModel
import com.victor.loclarm2.presentation.home.screens.BottomNavigationBar
import com.victor.loclarm2.utils.GlassAlarmItem
import com.airbnb.lottie.compose.*
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
    var showDeleteConfirmSheet by remember { mutableStateOf(false) }
    var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }
    val context = LocalContext.current

    NetworkAwareContent {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Alarms") }) },
            bottomBar = { BottomNavigationBar(navController, homeViewModel) }
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {

                if (alarms.isEmpty()) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.no_data_lottie))
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.size(250.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No alarms set yet", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
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
                                onDelete = {
                                    alarmToDelete = alarm
                                    showDeleteConfirmSheet = true
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
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
                            viewModel.updateAlarm(context, updatedAlarm)
                            showBottomSheet = false
                            selectedAlarm = null
                        }
                    )
                }

                if (showDeleteConfirmSheet && alarmToDelete != null) {
                    ConfirmDeleteBottomSheet(
                        alarm = alarmToDelete!!,
                        onConfirm = {
                            viewModel.deleteAlarm(alarmToDelete!!.id)
                            showDeleteConfirmSheet = false
                            alarmToDelete = null
                        },
                        onDismiss = {
                            showDeleteConfirmSheet = false
                            alarmToDelete = null
                        }
                    )
                }
            }
        }
    }
}