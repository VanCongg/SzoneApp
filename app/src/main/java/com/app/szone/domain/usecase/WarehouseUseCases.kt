package com.app.szone.domain.usecase

import com.app.szone.domain.repository.WarehouseRepository

class GetWarehouseInfoUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    suspend operator fun invoke(scannerId: String) = warehouseRepository.getWarehouseInfo(scannerId)
}

class ScanOrderArrivedUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    suspend operator fun invoke(orderId: String) = warehouseRepository.scanOrderArrived(orderId)
}

class GetCachedWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    suspend operator fun invoke() = warehouseRepository.getCachedWarehouse()
}

