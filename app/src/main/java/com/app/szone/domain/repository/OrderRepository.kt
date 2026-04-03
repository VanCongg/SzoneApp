package com.app.szone.domain.repository

import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.WarehouseModel

interface OrderRepository {
    suspend fun getOrderDetails(orderId: String, shipperName: String, shipperPhone: String): Resource<OrderModel>
    suspend fun getWarehouseInfo(scannerId: String): Resource<WarehouseModel>
    suspend fun scanOrderArrived(orderId: String, warehouseName: String, warehouseAddress: String): Resource<Unit>
    suspend fun confirmDeliverySuccess(orderId: String, shopId: String): Resource<Unit>
    suspend fun confirmDeliveryFail(orderId: String): Resource<Unit>
    suspend fun syncPendingActions()
}

