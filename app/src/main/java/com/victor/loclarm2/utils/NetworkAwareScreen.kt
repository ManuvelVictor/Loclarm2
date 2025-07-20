package com.victor.loclarm2.utils

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.victor.loclarm2.R
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NetworkAwareContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current.applicationContext as Application
    val snackBarHostState = remember { SnackbarHostState() }
    val isConnected = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        NetworkMonitor.observe(context).collectLatest { connected ->
            isConnected.value = connected
            if (!connected) {
                snackBarHostState.showSnackbar("No internet connection")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = Color.Transparent, // allow full background drawing
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isConnected.value) {
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.no_internet_connection)
                )
                val progress by animateLottieCompositionAsState(
                    composition,
                    iterations = LottieConstants.IterateForever
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(250.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("You're offline", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                content()
            }
        }
    }
}