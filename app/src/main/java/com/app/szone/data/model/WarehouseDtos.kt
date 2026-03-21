package com.app.szone.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseResponse(
    val id: String,
    val name: String,
    val address: String
)

@Serializable
data class ArrivedWarehouseRequest(
    val name: String,
    val address: String
)

