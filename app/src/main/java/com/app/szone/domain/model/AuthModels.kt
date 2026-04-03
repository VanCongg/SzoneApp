package com.app.szone.domain.model

import java.util.Locale

enum class UserRole {
    SHIPPER,
    WAREHOUSE_SCANNER,
    UNKNOWN;

    companion object {
        fun fromRaw(raw: String?): UserRole {
            return when (raw?.trim()?.uppercase(Locale.ROOT)) {
                SHIPPER.name -> SHIPPER
                WAREHOUSE_SCANNER.name -> WAREHOUSE_SCANNER
                else -> UNKNOWN
            }
        }
    }
}

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String,
    val roleName: String, // "SHIPPER", "WAREHOUSE_SCANNER", "CUSTOMER"
    val avatar: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

