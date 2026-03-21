package com.app.szone.domain.usecase

import com.app.szone.domain.repository.OrderRepository

class UpdateDeliveryStatusUseCase(
    private val orderRepository: OrderRepository
) {
    suspend fun success(orderId: String, shopId: String) =
        orderRepository.confirmDeliverySuccess(orderId, shopId)

    suspend fun fail(orderId: String) =
        orderRepository.confirmDeliveryFail(orderId)
}

