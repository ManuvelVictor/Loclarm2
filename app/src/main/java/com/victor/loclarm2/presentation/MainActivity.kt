package com.victor.loclarm2.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        lifecycleScope.launch {
            val startDestination = if (dataStoreManager.isLoggedIn()) "home" else "login"

            setContent {
                Loclarm2Theme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("login") { LoginScreen(navController) }
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
