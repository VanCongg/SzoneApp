package com.app.szone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val recipientName: String,
    val recipientPhone: String,
    val recipientAddress: String,
    val shopId: String,
    val shopName: String,
    val shopPhone: String,
    val shopAddress: String,
    val shippingFee: Long,
    val price: Long,
    val productsJson: String,
    val localStatus: String = "NONE",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_delivery_actions")
data class PendingDeliveryActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val actionType: String,
    val shopId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

