package com.victor.loclarm2.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.victor.loclarm2.data.local.DataStoreManager
import com.victor.loclarm2.presentation.alarm.screens.AlarmsScreen
import com.victor.loclarm2.presentation.auth.screens.LoginScreen
import com.victor.loclarm2.presentation.auth.screens.RegisterScreen
import com.victor.loclarm2.presentation.home.screens.HomeScreen
import com.victor.loclarm2.presentation.settings.screens.SettingsScreen
import com.victor.loclarm2.ui.theme.Loclarm2Theme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Loclarm2Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var isLoading by remember { mutableStateOf(true) }
                    var startDestination by remember { mutableStateOf("login") }

                    LaunchedEffect(Unit) {
                        startDestination = try {
                            if (dataStoreManager.isLoggedIn()) "home" else "login"
                        } catch (_: Exception) {
                            "login"
                        }
                        isLoading = false
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("login") {
                                LoginScreen(navController)
                            }
                            composable("register") { RegisterScreen(navController) }
                            composable("home") { HomeScreen(navController) }
                            composable("alarms") { AlarmsScreen(navController) }
                            composable("settings") { SettingsScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}