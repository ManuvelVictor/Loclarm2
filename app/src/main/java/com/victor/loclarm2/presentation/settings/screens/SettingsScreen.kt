package com.victor.loclarm2.presentation.settings.screens

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.presentation.auth.viewmodel.AuthViewModel
import com.victor.loclarm2.presentation.home.screens.BottomNavigationBar
import com.victor.loclarm2.presentation.home.viewmodel.HomeViewModel
import com.victor.loclarm2.presentation.settings.viewmodel.SettingsViewModel
import com.victor.loclarm2.utils.NetworkAwareContent
import com.victor.loclarm2.utils.SectionHeader
import com.victor.loclarm2.utils.SettingsCard
import com.victor.loclarm2.utils.SettingsItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: SettingsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val language by viewModel.language.collectAsState()
    val units by viewModel.units.collectAsState()
    val ringtone by viewModel.ringtone.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val vibration by viewModel.vibration.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showUnitsSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            uri?.let { viewModel.setRingtone(it.toString()) }
        }
    }

    NetworkAwareContent {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    actions = {
                        TextButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                )
            },
            bottomBar = { BottomNavigationBar(navController, homeViewModel) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SectionHeader(title = "General")
                SettingsCard {
                    SettingsItem(
                        label = "Language",
                        value = language,
                        actionLabel = "Change"
                    ) {
                        showLanguageSheet = true
                    }

                    SettingsItem(
                        label = "Units",
                        value = units,
                        actionLabel = "Change"
                    ) {
                        showUnitsSheet = true
                    }

                }

                SectionHeader(title = "Alarm")
                SettingsCard {
                    SettingsItem(
                        label = "Ringtone",
                        value = ringtone,
                        actionLabel = "Change"
                    ) {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(
                                RingtoneManager.EXTRA_RINGTONE_TYPE,
                                RingtoneManager.TYPE_NOTIFICATION
                            )
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        }
                        ringtonePickerLauncher.launch(intent)
                    }

                    Text("Volume", style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = volume,
                        onValueChange = viewModel::setVolume,
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vibration", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = vibration,
                            onCheckedChange = viewModel::setVibration
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            viewModel.saveSettings(language, units, ringtone, volume, vibration)
                            showSuccessDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text("Success") },
                text = { Text("Settings saved successfully.") },
                confirmButton = {
                    TextButton(onClick = { showSuccessDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showLanguageSheet) {
            ModalBottomSheet(onDismissRequest = { showLanguageSheet = false }) {
                Column(Modifier.padding(16.dp)) {
                    listOf("English", "தமிழ்", "Deutsch").forEach { lang ->
                        TextButton(onClick = {
                            scope.launch {
                                viewModel.saveSettings(lang, units, ringtone, volume, vibration)
                            }
                            showLanguageSheet = false
                        }) {
                            Text(lang)
                        }
                    }
                }
            }
        }

        if (showUnitsSheet) {
            ModalBottomSheet(onDismissRequest = { showUnitsSheet = false }) {
                Column(Modifier.padding(16.dp)) {
                    listOf("Metric", "Imperial").forEach { unit ->
                        TextButton(onClick = {
                            scope.launch {
                                viewModel.saveSettings(language, unit, ringtone, volume, vibration)
                            }
                            showUnitsSheet = false
                        }) {
                            Text(unit)
                        }
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            authViewModel.logout()
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                        showLogoutDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}