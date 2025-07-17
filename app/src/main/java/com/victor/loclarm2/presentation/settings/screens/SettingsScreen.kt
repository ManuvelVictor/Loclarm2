package com.victor.loclarm2.presentation.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.presentation.auth.viewmodel.AuthViewModel
import com.victor.loclarm2.presentation.home.screens.BottomNavigationBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var selectedLanguage by remember { mutableStateOf("English") }
    var selectedUnits by remember { mutableStateOf("Metric") }
    var ringtone by remember { mutableStateOf("Select Ringtone") }
    var volume by remember { mutableStateOf(5f) }
    var vibration by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                actions = {
                    TextButton(onClick = {
                        // Save logic
                    }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("General", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            Text("Language", style = MaterialTheme.typography.bodyLarge)
            Text(selectedLanguage, modifier = Modifier.padding(start = 8.dp, bottom = 16.dp))

            Text("Units", style = MaterialTheme.typography.bodyLarge)
            Text(selectedUnits, modifier = Modifier.padding(start = 8.dp, bottom = 16.dp))

            Divider()

            Spacer(Modifier.height(16.dp))
            Text("Alarm", color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ringtone", style = MaterialTheme.typography.bodyLarge)
                TextButton(onClick = { /* TODO: open ringtone picker */ }) {
                    Text(ringtone, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Volume", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = volume,
                onValueChange = { volume = it },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vibration", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = vibration, onCheckedChange = { vibration = it })
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
