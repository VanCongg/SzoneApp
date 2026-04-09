package com.app.szone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String = "", // ✅ Default empty string để tránh null
    val roleName: String,
    val username: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val avatar: String? = null,
    val status: String? = null,
    val require2FA: Boolean = false,
    val emailVerified: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

