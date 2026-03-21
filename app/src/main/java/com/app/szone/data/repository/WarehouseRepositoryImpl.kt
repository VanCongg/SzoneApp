package com.app.szone.data.repository

import com.app.szone.data.local.dao.WarehouseDao
import com.app.szone.data.mapping.toDomain
import com.app.szone.data.mapping.toEntity
import com.app.szone.data.model.ArrivedWarehouseRequest
import com.app.szone.data.remote.OrderService
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.WarehouseModel
import com.app.szone.domain.repository.WarehouseRepository

class WarehouseRepositoryImpl(
    private val orderService: OrderService,
    private val warehouseDao: WarehouseDao
) : WarehouseRepository {

    override suspend fun getWarehouseInfo(scannerId: String): Resource<WarehouseModel> {
        return try {
            val response = orderService.getWarehouseInfo(scannerId)
            if (response.success && response.data != null) {
                warehouseDao.upsertWarehouse(response.data.toEntity())
                Resource.Success(response.data.toDomain())
            } else {
                val cached = warehouseDao.getWarehouse()
                if (cached != null) {
                    Resource.Success(cached.toDomain())
                } else {
                    Resource.Error(mapError(response.code, response.message), response.code)
                }
            }
        } catch (e: Exception) {
            val cached = warehouseDao.getWarehouse()
            if (cached != null) {
                Resource.Success(cached.toDomain())
            } else {
                Resource.Error(e.message ?: "Không thể lấy thông tin kho", null)
            }
        }
    }

    override suspend fun getCachedWarehouse(): WarehouseModel? {
        return warehouseDao.getWarehouse()?.toDomain()
    }

    override suspend fun scanOrderArrived(orderId: String): Resource<Unit> {
        val warehouse = warehouseDao.getWarehouse()
            ?: return Resource.Error("Chưa có thông tin kho, vui lòng tải lại", null)

        return try {
            val response = orderService.arrivedWarehouse(
                orderId = orderId,
                body = ArrivedWarehouseRequest(name = warehouse.name, address = warehouse.address)
            )
            if (response.success) {
                Resource.Success(Unit)
            } else {
                Resource.Error(mapError(response.code, response.message), response.code)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Quét thất bại, vui lòng thử lại", null)
        }
    }

    private fun mapError(code: Int?, message: String?): String {
        return when (code) {
            401 -> "Token hết hạn, vui lòng đăng nhập lại"
            404 -> "Không tìm thấy đơn hàng hoặc scanner"
            403 -> "Không có quyền thao tác"
            500 -> "Lỗi hệ thống, vui lòng thử lại sau"
            else -> message ?: "Thao tác thất bại"
        }
    }
}

