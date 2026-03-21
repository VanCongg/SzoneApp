package com.app.szone.domain.repository

import com.app.szone.domain.model.AuthResponse
import com.app.szone.domain.model.LoginRequest
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: LoginRequest): Resource<AuthResponse>
    suspend fun logout(): Resource<Unit>
    suspend fun getCurrentUser(): Resource<User>
    fun isLoggedIn(): Flow<Boolean>
    fun getCurrentToken(): Flow<String?>
}

