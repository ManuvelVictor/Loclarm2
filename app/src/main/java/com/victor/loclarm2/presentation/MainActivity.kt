package com.victor.loclarm2.presentation

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.victor.loclarm2.data.model.Alarm
import com.victor.loclarm2.presentation.alarm.screens.AlarmsScreen
import com.victor.loclarm2.presentation.auth.screens.LoginScreen
import com.victor.loclarm2.presentation.auth.screens.RegisterScreen
import com.victor.loclarm2.presentation.home.screens.HomeScreen
import com.victor.loclarm2.presentation.home.viewmodel.HomeViewModel
import com.victor.loclarm2.presentation.settings.screens.SettingsScreen
import com.victor.loclarm2.presentation.ui.theme.Loclarm2Theme
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    private val homeViewModel: HomeViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Loclarm2Theme {
                EdgeToEdgeSystemBars()
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

                        if (startDestination == "home") {
                            handleAlarmDialogIntent()
                        }
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
                            composable("register") {
                                RegisterScreen(navController)
                            }
                            composable("home") {
                                HomeScreen(navController, homeViewModel)

                                LaunchedEffect(Unit) {
                                    handleAlarmDialogIntent()
                                }
                            }
                            composable("alarms") {
                                AlarmsScreen(navController)
                            }
                            composable("settings") {
                                SettingsScreen(navController)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmDialogIntent()
    }

    private fun handleAlarmDialogIntent() {
        val showAlarmDialog = intent.getBooleanExtra("SHOW_ALARM_DIALOG", false)
        if (showAlarmDialog) {
            val alarmId = intent.getStringExtra("ALARM_ID") ?: ""
            val alarmName = intent.getStringExtra("ALARM_NAME") ?: ""

            if (alarmId.isNotEmpty() && alarmName.isNotEmpty()) {
                val alarm = Alarm(
                    id = alarmId,
                    name = alarmName,
                    latitude = 0.0,
                    longitude = 0.0,
                    radius = 0f,
                    userId = "",
                    active = true
                )
                homeViewModel.showAlarmDialog(alarm)
            }
        }
    }
}

@Composable
fun EdgeToEdgeSystemBars() {
    val isDarkTheme = isSystemInDarkTheme()
    val view = androidx.compose.ui.platform.LocalView.current

    SideEffect {
        val window = (view.context as ComponentActivity).window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            controller?.setSystemBarsAppearance(
                if (!isDarkTheme) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            val flags = if (!isDarkTheme) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else 0
            view.systemUiVisibility = flags
        }
    }
}