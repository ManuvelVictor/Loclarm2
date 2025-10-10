package com.victor.loclarm2.presentation.settings.screens

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.R
import com.victor.loclarm2.presentation.auth.viewmodel.AuthViewModel
import com.victor.loclarm2.presentation.home.screens.BottomNavigationBar
import com.victor.loclarm2.presentation.home.viewmodel.HomeViewModel
import com.victor.loclarm2.presentation.settings.viewmodel.SettingsViewModel
import com.victor.loclarm2.utils.NetworkAwareContent
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
    var showRingtoneSheet by remember { mutableStateOf(false) }

    val availableRingtones = listOf(
        "Ringtone 1" to R.raw.default_alarm_1,
        "Ringtone 2" to R.raw.default_alarm_2,
        "Ringtone 3" to R.raw.default_alarm_3,
        "Ringtone 4" to R.raw.default_alarm_4
    )

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
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    actions = {
                        FilledTonalIconButton(
                            onClick = { showLogoutDialog = true },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = { BottomNavigationBar(navController, homeViewModel) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    SectionHeader(title = "General", icon = Icons.Default.Settings)
                }

                item {
                    ModernSettingsCard {
                        ModernSettingsItem(
                            icon = Icons.Default.Language,
                            label = "Language",
                            value = language,
                            onClick = { showLanguageSheet = true }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ModernSettingsItem(
                            icon = Icons.Default.Place,
                            label = "Units",
                            value = units,
                            onClick = { showUnitsSheet = true }
                        )
                    }
                }

                item {
                    SectionHeader(title = "Alarm", icon = Icons.Default.Notifications)
                }

                item {
                    ModernSettingsCard {
                        ModernSettingsItem(
                            icon = Icons.Default.MusicNote,
                            label = "Ringtone",
                            value = ringtone.takeIf { it.contains("default_alarm") }?.let {
                                when {
                                    it.contains("1") -> "Ringtone 1"
                                    it.contains("2") -> "Ringtone 2"
                                    it.contains("3") -> "Ringtone 3"
                                    it.contains("4") -> "Ringtone 4"
                                    else -> "Default"
                                }
                            } ?: ringtone,
                            onClick = { showRingtoneSheet = true }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        "Volume",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${volume.toInt()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Slider(
                                value = volume,
                                onValueChange = viewModel::setVolume,
                                valueRange = 0f..10f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setVibration(!vibration) }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    "Vibration",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = vibration,
                                onCheckedChange = viewModel::setVibration
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                viewModel.saveSettings(language, units, ringtone, volume, vibration)
                                showSuccessDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        "Success",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        "Settings saved successfully.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    FilledTonalButton(onClick = { showSuccessDialog = false }) {
                        Text("OK")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showLanguageSheet) {
            ModernBottomSheet(
                onDismissRequest = { showLanguageSheet = false },
                title = "Select Language",
                icon = Icons.Default.Language
            ) {
                val languages = listOf(
                    "English" to Icons.Default.Language,
                    "தமிழ்" to Icons.Default.Language,
                    "Deutsch" to Icons.Default.Language
                )

                languages.forEach { (lang, icon) ->
                    ListItem(
                        headlineContent = {
                            Text(
                                lang,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            if (lang == language) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    viewModel.saveSettings(lang, units, ringtone, volume, vibration)
                                }
                                showLanguageSheet = false
                            }
                    )
                }
            }
        }

        if (showUnitsSheet) {
            ModernBottomSheet(
                onDismissRequest = { showUnitsSheet = false },
                title = "Select Units",
                icon = Icons.Default.Straighten
            ) {
                val unitOptions = listOf(
                    "Metric" to Icons.Default.Straighten,
                    "Imperial" to Icons.Default.Straighten
                )

                unitOptions.forEach { (unit, icon) ->
                    ListItem(
                        headlineContent = {
                            Text(
                                unit,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        supportingContent = {
                            Text(
                                if (unit == "Metric") "Kilometers, Celsius" else "Miles, Fahrenheit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            if (unit == units) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    viewModel.saveSettings(
                                        language,
                                        unit,
                                        ringtone,
                                        volume,
                                        vibration
                                    )
                                }
                                showUnitsSheet = false
                            }
                    )
                }
            }
        }

        if (showRingtoneSheet) {
            ModernBottomSheet(
                onDismissRequest = { showRingtoneSheet = false },
                title = "Select Ringtone",
                icon = Icons.Default.MusicNote
            ) {
                availableRingtones.forEach { (name, resId) ->
                    ListItem(
                        headlineContent = {
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            if (ringtone.contains(resId.toString())) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch {
                                    viewModel.saveSettings(
                                        language,
                                        units,
                                        resId.toString(),
                                        volume,
                                        vibration
                                    )
                                }
                                showRingtoneSheet = false
                            }
                    )
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        "Logout",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to logout?",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    FilledTonalButton(
                        onClick = {
                            scope.launch {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                }
                            }
                            showLogoutDialog = false
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
