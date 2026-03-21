package com.app.szone.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    @SerialName("_id")
    val id: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String,
    val phone: String,
    @SerialName("role_name")
    val roleName: String,
    val avatar: String? = null,
    val status: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val code: Int? = null
)

