package com.app.szone.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShipperInfoDto(
    val name: String,
    val phoneNumber: String
)

@Serializable
data class ShopIdBodyDto(
    val shopId: String
)

@Serializable
data class OrderEnvelopeDto(
    val order: OrderDto
)

@Serializable
data class OrderDto(
    val id: String,
    val recipient: RecipientDto,
    val shop: ShopDto,
    val shippingFee: Long,
    val price: Long,
    val productList: List<ProductDto>
)

@Serializable
data class RecipientDto(
    val name: String,
    val phoneNumber: String,
    val address: String
)

@Serializable
data class ShopDto(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val address: String
)

@Serializable
data class ProductDto(
    val name: String,
    val sku: String,
    val quantity: Int
)

