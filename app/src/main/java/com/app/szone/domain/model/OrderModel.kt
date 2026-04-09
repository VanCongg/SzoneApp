package com.app.szone.domain.model

data class OrderModel(
    val id: String,
    val recipient: RecipientModel,
    val shop: ShopModel,
    val shippingFee: Double,
    val price: Double,
    val productList: List<ProductModel>,
    val localStatus: String = "NONE"
) {
    val totalMoney: Double
        get() = price + shippingFee
}

data class RecipientModel(
    val name: String,
    val phoneNumber: String,
    val address: String
)

data class ShopModel(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val address: String
)

data class ProductModel(
    val name: String,
    val sku: String,
    val quantity: Int
)

