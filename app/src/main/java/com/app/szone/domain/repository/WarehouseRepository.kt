package com.app.szone.domain.repository

import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.WarehouseModel

interface WarehouseRepository {
    suspend fun getWarehouseInfo(scannerId: String): Resource<WarehouseModel>
    suspend fun getCachedWarehouse(): WarehouseModel?
    suspend fun scanOrderArrived(orderId: String): Resource<Unit>
}

