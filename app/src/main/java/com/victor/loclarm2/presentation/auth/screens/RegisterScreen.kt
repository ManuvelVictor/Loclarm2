package com.victor.loclarm2.presentation.auth.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.R
import com.victor.loclarm2.presentation.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import com.victor.loclarm2.utils.GlassTextField

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val errorMessage = viewModel.errorMessage.collectAsState()
    val user = viewModel.user.collectAsState()

    val isLoading = viewModel.isLoading.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(user.value, errorMessage.value) {
        if (user.value != null) {
            dialogMessage = "Registration successful!"
            showDialog = true
        } else if (!errorMessage.value.isNullOrEmpty()) {
            dialogMessage = errorMessage.value ?: "Something went wrong"
            showDialog = true
        }
    }

    LaunchedEffect(user.value) {
        if (user.value != null) {
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Get Started", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.register_vector),
            contentDescription = "Register illustration",
            modifier = Modifier
                .height(250.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassTextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = "Username",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = "Email",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        GlassTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = "Password",
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        errorMessage.value?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    viewModel.register(email.value, password.value, username.value)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login")
        }

        if (isLoading.value) {
            Dialog(onDismissRequest = {}) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White, shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    if (user.value != null) {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        if (user.value != null) {
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    }) {
                        Text("OK")
                    }
                },
                title = { Text("Registration") },
                text = { Text(dialogMessage) }
            )
        }

    }
}