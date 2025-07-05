package com.victor.loclarm2.domain.repository

import android.content.Context
import com.victor.loclarm2.data.model.User

interface AuthRepository {
    suspend fun register(email: String, password: String, username: String): Result<User>
    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun loginWithGoogle(context: Context): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout()
}