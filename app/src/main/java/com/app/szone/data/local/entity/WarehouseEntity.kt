package com.app.szone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warehouse")
data class WarehouseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String
)

