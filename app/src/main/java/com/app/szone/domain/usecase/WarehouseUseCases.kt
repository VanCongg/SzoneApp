package com.app.szone.domain.usecase

import com.app.szone.domain.model.WarehouseModel
import com.app.szone.domain.repository.WarehouseRepository
import com.app.szone.domain.repository.OrderRepository

class GetWarehouseInfoUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    suspend operator fun invoke(scannerId: String) = warehouseRepository.getWarehouseInfo(scannerId)
}

class ScanOrderArrivedUseCase(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, warehouseName: String, warehouseAddress: String) =
        orderRepository.scanOrderArrived(orderId, warehouseName, warehouseAddress)
}

class GetCachedWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    suspend operator fun invoke() = warehouseRepository.getCachedWarehouse()
}

