package com.app.szone.domain.repository

import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.Resource

interface OrderRepository {
    suspend fun getOrderDetails(orderId: String, shipperName: String, shipperPhone: String): Resource<OrderModel>
    suspend fun confirmDeliverySuccess(orderId: String, shopId: String): Resource<Unit>
    suspend fun confirmDeliveryFail(orderId: String): Resource<Unit>
    suspend fun syncPendingActions()
}

