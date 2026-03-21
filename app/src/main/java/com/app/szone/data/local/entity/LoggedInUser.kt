package com.app.szone.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "logged_in_user")
data class LoggedInUser(
    @PrimaryKey
    val _id: String,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val address: String,
    val dob: String,
    val avatar: String,
    val roles: String,
    val status: String,
    val require2FA: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

