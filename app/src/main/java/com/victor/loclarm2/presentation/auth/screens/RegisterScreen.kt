package com.victor.loclarm2.presentation.auth.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.victor.loclarm2.R
import com.victor.loclarm2.presentation.auth.viewmodel.AuthViewModel
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
    val errorMessage by viewModel.errorMessage.collectAsState()
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(user, errorMessage) {
        when {
            user != null -> {
                dialogMessage = "Registration successful!"
                showDialog = true
            }
            !errorMessage.isNullOrEmpty() -> {
                dialogMessage = errorMessage ?: "Something went wrong"
                showDialog = true
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
            Text("Get Started", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.register_vector),
                contentDescription = null,
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    scope.launch { viewModel.register(email.value, password.value, username.value) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Already have an account? Login")
            }

            if (isLoading) {
                Dialog(onDismissRequest = {}) {
                    Box(
                        modifier = Modifier.size(100.dp),
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
                        if (user != null) {
                            navController.navigate("home") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            if (user != null) {
                                navController.navigate("home") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                        }) {
                            Text("OK")
                        }
                    },
                    title = { Text("Registration Status") },
                    text = { Text(dialogMessage) }
                )
            }
        }
    }
}