package com.app.szone.data.mapping

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.app.szone.data.local.entity.OrderEntity
import com.app.szone.data.model.OrderDto
import com.app.szone.data.model.ProductDto
import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.ProductModel
import com.app.szone.domain.model.RecipientModel
import com.app.szone.domain.model.ShopModel

private val gson = Gson()

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
        productsJson = gson.toJson(productList),
        localStatus = localStatus
    )
}

fun OrderEntity.toDomain(): OrderModel {
    val productType = object : TypeToken<List<ProductDto>>() {}.type
    val products = gson.fromJson<List<ProductDto>>(productsJson, productType).orEmpty()
    return OrderModel(
        id = id,
        recipient = RecipientModel(recipientName, recipientPhone, recipientAddress),
        shop = ShopModel(shopId, shopName, shopPhone, shopAddress),
        shippingFee = shippingFee,
        price = price,
        productList = products.map { ProductModel(it.name, it.sku, it.quantity) },
        localStatus = localStatus
    )
}

