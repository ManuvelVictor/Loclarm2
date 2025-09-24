package com.victor.loclarm2.presentation.auth.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.R
import com.victor.loclarm2.presentation.auth.viewmodel.AuthViewModel
import com.victor.loclarm2.utils.GlassTextField
import com.victor.loclarm2.utils.NetworkAwareContent
import kotlinx.coroutines.launch

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

    NetworkAwareContent {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
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
                modifier = Modifier.fillMaxWidth(),
                isPassword = false
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
                isPassword = false
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                isPassword = true
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
                            .clip(MaterialTheme.shapes.medium)
                            .padding(20.dp),
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
}