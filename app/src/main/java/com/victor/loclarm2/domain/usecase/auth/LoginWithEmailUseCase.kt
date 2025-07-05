package com.victor.loclarm2.domain.usecase.auth

import com.victor.loclarm2.data.model.User
import com.victor.loclarm2.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.loginWithEmail(email, password)
    }
}