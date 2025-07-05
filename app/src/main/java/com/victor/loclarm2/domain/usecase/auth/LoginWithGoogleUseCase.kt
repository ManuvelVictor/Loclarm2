package com.victor.loclarm2.domain.usecase.auth

import android.content.Context
import com.victor.loclarm2.data.model.User
import com.victor.loclarm2.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(context: Context): Result<User> {
        return authRepository.loginWithGoogle(context)
    }
}