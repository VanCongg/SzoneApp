package com.app.szone.domain.usecase

import com.app.szone.domain.model.AuthResponse
import com.app.szone.domain.model.LoginRequest
import com.app.szone.domain.model.Resource
import com.app.szone.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<AuthResponse> {
        val request = LoginRequest(email, password)
        return authRepository.login(request)
    }
}

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.logout()
}

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.getCurrentUser()
}

class IsLoggedInUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() = authRepository.isLoggedIn()
}

class GetCurrentTokenUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() = authRepository.getCurrentToken()
}

