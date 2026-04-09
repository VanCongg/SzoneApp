package com.app.szone.data.repository

import com.app.szone.data.local.dao.OrderDao
import com.app.szone.data.local.dao.PendingDeliveryActionDao
import com.app.szone.data.local.entity.PendingDeliveryActionEntity
import com.app.szone.data.mapping.toDomain
import com.app.szone.data.mapping.toEntity
import com.app.szone.data.model.ArrivedWarehouseRequest
import com.app.szone.data.model.ShipperInfoDto
import com.app.szone.data.model.SuccessRequest
import com.app.szone.data.remote.OrderService
import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.WarehouseModel
import com.app.szone.domain.repository.OrderRepository
import kotlinx.serialization.SerializationException

class OrderRepositoryImpl(
    private val orderService: OrderService,
    private val orderDao: OrderDao,
    private val pendingActionDao: PendingDeliveryActionDao
) : OrderRepository {

    override suspend fun getOrderDetails(orderId: String, shipperName: String, shipperPhone: String): Resource<OrderModel> {
        return try {
            android.util.Log.d("OrderRepository", "🔄 Calling getOrderToShipper with orderId=$orderId, name=$shipperName, phone=$shipperPhone")
            
            val response = orderService.getOrderToShipper(
                orderId = orderId,
                shipperInfo = ShipperInfoDto(name = shipperName, phoneNumber = shipperPhone)
            )

            android.util.Log.d("OrderRepository", "✅ Response received: success=${response.success}, code=${response.code}")

            // ✅ Log full response data for debugging
            if (response.data != null) {
                val order = response.data.order
                android.util.Log.d("OrderRepository", "📦 Order Data:")
                android.util.Log.d("OrderRepository", "  - id: ${order.id}")
                android.util.Log.d("OrderRepository", "  - price: ${order.price}")
                android.util.Log.d("OrderRepository", "  - shippingFee: ${order.shippingFee}")
                android.util.Log.d("OrderRepository", "  - recipient: ${order.recipient}")
                android.util.Log.d("OrderRepository", "  - shop: ${order.shop}")
                android.util.Log.d("OrderRepository", "  - productList size: ${order.productList.size}")
            }

            if (response.success && response.data != null) {
                val order = response.data.order
                orderDao.upsertOrder(order.toEntity())
                android.util.Log.d("OrderRepository", "✅ Order saved to DB: ${order.id}")
                Resource.Success(order.toDomain())
            } else {
                android.util.Log.w("OrderRepository", "❌ API returned error: ${response.message}")
                val cached = orderDao.getOrderById(orderId)
                if (cached != null) {
                    Resource.Success(cached.toDomain())
                } else {
                    Resource.Error(mapApiError(response.code, response.message), response.code)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "❌ Exception fetching order: ${e.message}", e)
            android.util.Log.e("OrderRepository", "❌ Exception type: ${e::class.simpleName}")

            // Log detailed error info
            if (e is kotlinx.serialization.SerializationException) {
                android.util.Log.e("OrderRepository", "❌ Serialization Error - JSON parsing failed!")
                android.util.Log.e("OrderRepository", "❌ Details: ${e.message}")
            }

            val cached = orderDao.getOrderById(orderId)
            if (cached != null) {
                Resource.Success(cached.toDomain())
            } else {
                Resource.Error(e.message ?: "Mất kết nối", null)
            }
        }
    }

    override suspend fun getWarehouseInfo(scannerId: String): Resource<WarehouseModel> {
        return try {
            val response = orderService.getWarehouseInfo(scannerId)
            if (response.success && response.data != null) {
                val warehouse = response.data
                Resource.Success(
                    WarehouseModel(
                        id = warehouse.id,
                        name = warehouse.name,
                        address = warehouse.address
                    )
                )
            } else {
                Resource.Error(mapApiError(response.code, response.message), response.code)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Mất kết nối", null)
        }
    }

    override suspend fun scanOrderArrived(orderId: String, warehouseName: String, warehouseAddress: String): Resource<Unit> {
        return try {
            val response = orderService.arrivedWarehouse(
                orderId = orderId,
                body = ArrivedWarehouseRequest(name = warehouseName, address = warehouseAddress)
            )
            if (response.success) {
                Resource.Success(Unit)
            } else {
                Resource.Error(mapApiError(response.code, response.message), response.code)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Mất kết nối", null)
        }
    }

    override suspend fun confirmDeliverySuccess(orderId: String, shopId: String): Resource<Unit> {
        return try {
            val response = orderService.deliverySuccess(orderId, SuccessRequest(shopId))
            if (response.success) {
                orderDao.updateLocalStatus(orderId, "DELIVERED")
                Resource.Success(Unit)
            } else {
                Resource.Error(mapApiError(response.code, response.message), response.code)
            }
        } catch (_: Exception) {
            pendingActionDao.insertAction(
                PendingDeliveryActionEntity(orderId = orderId, actionType = "SUCCESS", shopId = shopId)
            )
            orderDao.updateLocalStatus(orderId, "PENDING_SUCCESS")
            Resource.Error("Mất mạng, đã lưu thao tác và sẽ tự đồng bộ", null)
        }
    }

    override suspend fun confirmDeliveryFail(orderId: String): Resource<Unit> {
        return try {
            val response = orderService.deliveryFail(orderId)
            if (response.success) {
                orderDao.updateLocalStatus(orderId, "FAILED")
                Resource.Success(Unit)
            } else {
                Resource.Error(mapApiError(response.code, response.message), response.code)
            }
        } catch (_: Exception) {
            pendingActionDao.insertAction(
                PendingDeliveryActionEntity(orderId = orderId, actionType = "FAIL")
            )
            orderDao.updateLocalStatus(orderId, "PENDING_FAIL")
            Resource.Error("Mất mạng, đã lưu thao tác và sẽ tự đồng bộ", null)
        }
    }

    override suspend fun syncPendingActions() {
        val pending = pendingActionDao.getPendingActions()
        pending.forEach { action ->
            try {
                when (action.actionType) {
                    "SUCCESS" -> {
                        val shopId = action.shopId ?: return@forEach
                        val response = orderService.deliverySuccess(action.orderId, SuccessRequest(shopId))
                        if (response.success) {
                            orderDao.updateLocalStatus(action.orderId, "DELIVERED")
                            pendingActionDao.deleteAction(action.id)
                        }
                    }

                    "FAIL" -> {
                        val response = orderService.deliveryFail(action.orderId)
                        if (response.success) {
                            orderDao.updateLocalStatus(action.orderId, "FAILED")
                            pendingActionDao.deleteAction(action.id)
                        }
                    }
                }
            } catch (_: Exception) {
                // keep queue for later sync
            }
        }
    }

    private fun mapApiError(code: Int?, message: String?): String {
        return when (code) {
            401 -> "Token hết hạn, vui lòng đăng nhập lại"
            404 -> "Đơn hàng không tồn tại"
            403 -> "Không có quyền cập nhật đơn hàng"
            500 -> "Lỗi hệ thống, vui lòng thử lại sau"
            else -> message ?: "Cập nhật đơn hàng thất bại"
        }
    }
}
