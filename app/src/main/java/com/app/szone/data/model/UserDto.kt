package com.app.szone.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LegacyRoleDto(
    val name: String,
)

@Serializable
data class LegacyUserDto(
    val _id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val address: String,
    val dob: String,
    val avatar: String,
    val roles: List<LegacyRoleDto>,
    val status: String,
    val require2FA: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
