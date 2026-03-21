package com.app.szone.domain.model

data class OrderModel(
    val id: String,
    val recipient: RecipientModel,
    val shop: ShopModel,
    val shippingFee: Long,
    val price: Long,
    val productList: List<ProductModel>,
    val localStatus: String = "NONE"
) {
    val totalMoney: Long
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

