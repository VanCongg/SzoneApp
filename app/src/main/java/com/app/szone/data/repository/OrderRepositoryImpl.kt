package com.app.szone.data.repository

import com.app.szone.data.local.dao.OrderDao
import com.app.szone.data.local.dao.PendingDeliveryActionDao
import com.app.szone.data.local.entity.PendingDeliveryActionEntity
import com.app.szone.data.mapping.toDomain
import com.app.szone.data.mapping.toEntity
import com.app.szone.data.model.ShipperInfoDto
import com.app.szone.data.model.SuccessRequest
import com.app.szone.data.remote.OrderService
import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.Resource
import com.app.szone.domain.repository.OrderRepository

class OrderRepositoryImpl(
    private val orderService: OrderService,
    private val orderDao: OrderDao,
    private val pendingActionDao: PendingDeliveryActionDao
) : OrderRepository {

    override suspend fun getOrderDetails(orderId: String, shipperName: String, shipperPhone: String): Resource<OrderModel> {
        return try {
            syncPendingActions()
            val response = orderService.getOrderToShipper(
                orderId = orderId,
                shipperInfo = ShipperInfoDto(name = shipperName, phoneNumber = shipperPhone)
            )

            if (response.success && response.data != null) {
                val order = response.data.order
                orderDao.upsertOrder(order.toEntity())
                Resource.Success(order.toDomain())
            } else {
                val cached = orderDao.getOrderById(orderId)
                if (cached != null) {
                    Resource.Success(cached.toDomain())
                } else {
                    Resource.Error(mapApiError(response.code, response.message), response.code)
                }
            }
        } catch (e: Exception) {
            val cached = orderDao.getOrderById(orderId)
            if (cached != null) {
                Resource.Success(cached.toDomain())
            } else {
                Resource.Error(e.message ?: "Mất kết nối", null)
            }
        }
    }

    override suspend fun confirmDeliverySuccess(orderId: String, shopId: String): Resource<Unit> {
        return try {
            syncPendingActions()
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
            syncPendingActions()
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
