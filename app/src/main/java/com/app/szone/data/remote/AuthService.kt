package com.app.szone.data.remote

import com.app.szone.data.model.ApiResponse
import com.app.szone.data.model.AuthResponseDto
import com.app.szone.data.model.LoginRequestDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiResponse<AuthResponseDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Header("Authorization") bearerToken: String): ApiResponse<Unit>
}

