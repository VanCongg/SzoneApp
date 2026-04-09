package com.app.szone.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val username: String? = null,
    val email: String,
    val fullName: String,
    val phoneNumber: String = "",  // ✅ Default empty string
    val roleName: String,
    val dob: String? = null,
    val gender: String? = null,
    val avatar: String? = null,
    val status: String? = null,
    val require2FA: Boolean = false,
    val emailVerified: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val code: Int? = null
)

