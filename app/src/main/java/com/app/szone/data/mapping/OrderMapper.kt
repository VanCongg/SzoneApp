package com.app.szone.data.mapping

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.app.szone.data.local.entity.OrderEntity
import com.app.szone.data.model.OrderDto
import com.app.szone.data.model.ProductDto
import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.ProductModel
import com.app.szone.domain.model.RecipientModel
import com.app.szone.domain.model.ShopModel

private val json = Json { ignoreUnknownKeys = true }

fun OrderDto.toDomain(localStatus: String = "NONE"): OrderModel {
    return OrderModel(
        id = id,
        recipient = RecipientModel(
            name = recipient.name,
            phoneNumber = recipient.phoneNumber,
            address = recipient.address
        ),
        shop = ShopModel(
            id = shop.id,
            name = shop.name,
            phoneNumber = shop.phoneNumber,
            address = shop.address
        ),
        shippingFee = shippingFee,
        price = price,
        productList = productList.map { ProductModel(it.name, it.sku, it.quantity) },
        localStatus = localStatus
    )
}

fun OrderDto.toEntity(localStatus: String = "NONE"): OrderEntity {
    return OrderEntity(
        id = id,
        recipientName = recipient.name,
        recipientPhone = recipient.phoneNumber,
        recipientAddress = recipient.address,
        shopId = shop.id,
        shopName = shop.name,
        shopPhone = shop.phoneNumber,
        shopAddress = shop.address,
        shippingFee = shippingFee,
        price = price,
        productsJson = json.encodeToString(productList),
        localStatus = localStatus
    )
}

fun OrderEntity.toDomain(): OrderModel {
    return try {
        val products = json.decodeFromString<List<ProductDto>>(productsJson)
        OrderModel(
            id = id,
            recipient = RecipientModel(recipientName, recipientPhone, recipientAddress),
            shop = ShopModel(shopId, shopName, shopPhone, shopAddress),
            shippingFee = shippingFee,
            price = price,
            productList = products.map { ProductModel(it.name, it.sku, it.quantity) },
            localStatus = localStatus
        )
    } catch (e: Exception) {
        // Fallback if JSON parsing fails
        OrderModel(
            id = id,
            recipient = RecipientModel(recipientName, recipientPhone, recipientAddress),
            shop = ShopModel(shopId, shopName, shopPhone, shopAddress),
            shippingFee = shippingFee,
            price = price,
            productList = emptyList(),
            localStatus = localStatus
        )
    }
}

