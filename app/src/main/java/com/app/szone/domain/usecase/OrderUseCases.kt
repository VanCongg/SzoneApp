package com.app.szone.domain.usecase

import com.app.szone.domain.repository.OrderRepository

class GetOrderDetailsUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String, shipperName: String, shipperPhone: String) =
        repository.getOrderDetails(orderId, shipperName, shipperPhone)
}

class ConfirmDeliverySuccessUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String, shopId: String) =
        repository.confirmDeliverySuccess(orderId, shopId)
}

class ConfirmDeliveryFailUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String) =
        repository.confirmDeliveryFail(orderId)
}

class SyncPendingDeliveryActionsUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke() = repository.syncPendingActions()
}

