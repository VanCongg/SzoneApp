package com.app.szone.data.mapping

import com.app.szone.data.local.entity.UserEntity
import com.app.szone.data.model.UserDto
import com.app.szone.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        fullName = fullName,
        phone = phoneNumber,
        roleName = roleName,
        avatar = avatar,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        fullName = fullName,
        phone = phoneNumber.takeIf { it.isNotBlank() } ?: "",
        roleName = roleName,
        username = username,
        dob = dob,
        gender = gender,
        avatar = avatar,
        status = status,
        require2FA = require2FA,
        emailVerified = emailVerified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        email = email,
        fullName = fullName,
        phone = phone.takeIf { it.isNotBlank() } ?: "",
        roleName = roleName,
        avatar = avatar,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

