package com.victor.loclarm2.domain.usecase.auth

import com.victor.loclarm2.data.model.User
import com.victor.loclarm2.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, username: String): Result<User> {
        return authRepository.register(email, password, username)
    }
}