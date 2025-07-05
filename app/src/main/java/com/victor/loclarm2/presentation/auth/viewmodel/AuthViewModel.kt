package com.victor.loclarm2.presentation.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victor.loclarm2.data.model.User
import com.victor.loclarm2.domain.repository.AuthRepository
import com.victor.loclarm2.domain.usecase.auth.LoginWithEmailUseCase
import com.victor.loclarm2.domain.usecase.auth.LoginWithGoogleUseCase
import com.victor.loclarm2.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val registerUseCase: RegisterUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        viewModelScope.launch {
            _user.value = authRepository.getCurrentUser()
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            registerUseCase(email, password, username).fold(
                onSuccess = { user ->
                    _user.value = user
                    _errorMessage.value = null
                },
                onFailure = { e ->
                    _errorMessage.value = e.message
                }
            )
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            loginWithEmailUseCase(email, password).fold(
                onSuccess = { user ->
                    _user.value = user
                    _errorMessage.value = null
                },
                onFailure = { e ->
                    _errorMessage.value = e.message
                }
            )
        }
    }

    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            loginWithGoogleUseCase(context).fold(
                onSuccess = { user ->
                    _user.value = user
                    _errorMessage.value = null
                },
                onFailure = { e ->
                    _errorMessage.value = e.message
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _user.value = null
        }
    }
}