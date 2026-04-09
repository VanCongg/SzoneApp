package com.app.szone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.Resource
import com.app.szone.domain.usecase.GetOrderDetailsUseCase
import com.app.szone.domain.usecase.UpdateDeliveryStatusUseCase
import com.app.szone.presentation.state.DeliveryUpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderUiState(
    val isLoading: Boolean = false,
    val order: OrderModel? = null,
    val orders: List<OrderModel> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val canShowActions: Boolean = false
)

/**
 * ViewModel for Order Management
 * Handles loading order details and updating delivery status
 * Implements MVVM pattern with clean state management
 */
class OrderViewModel(
    private val getOrderDetailsUseCase: GetOrderDetailsUseCase,
    private val updateDeliveryStatusUseCase: UpdateDeliveryStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<DeliveryUpdateState>(DeliveryUpdateState.Idle)
    val updateState: StateFlow<DeliveryUpdateState> = _updateState.asStateFlow()

    /**
     * Load order details by ID
     * Validates input before making API call
     */
    fun loadOrder(orderId: String, shipperName: String, shipperPhone: String) {
        if (orderId.isBlank()) {
            updateErrorState("QR code không hợp lệ")
            return
        }

        android.util.Log.d("OrderViewModel", "🔄 Loading order: $orderId")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = getOrderDetailsUseCase(orderId, shipperName, shipperPhone)) {
                is Resource.Success -> {
                    android.util.Log.d("OrderViewModel", "✅ Order loaded successfully: ${result.data?.id}")

                    // ✅ Add order to list if not already present
                    val updatedOrders = _uiState.value.orders.toMutableList()
                    if (result.data != null && updatedOrders.none { it.id == result.data.id }) {
                        updatedOrders.add(0, result.data) // Add to beginning
                        android.util.Log.d("OrderViewModel", "📦 Added to list. Total orders now: ${updatedOrders.size}")
                    } else {
                        android.util.Log.d("OrderViewModel", "⚠️ Order already in list or result is null")
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        order = result.data,
                        orders = updatedOrders,
                        canShowActions = true,
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    android.util.Log.e("OrderViewModel", "❌ Error loading order: ${result.error}")
                    updateErrorState(result.error)
                }
                is Resource.Loading -> {
                    android.util.Log.d("OrderViewModel", "⏳ Loading...")
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    /**
     * Confirm successful delivery
     * Updates order status and shows confirmation
     */
    fun confirmSuccess(orderId: String, shopId: String) {
        viewModelScope.launch {
            _updateState.value = DeliveryUpdateState.Loading
            when (val result = updateDeliveryStatusUseCase.success(orderId, shopId)) {
                is Resource.Success -> {
                    android.util.Log.d("OrderViewModel", "✅ Delivery success confirmed, removing order...")
                    removeOrder(orderId)  // ✅ Remove immediately
                    _updateState.value = DeliveryUpdateState.Success
                    _uiState.value = _uiState.value.copy(
                        order = null,
                        canShowActions = false,
                        successMessage = null
                    )
                }
                is Resource.Error -> {
                    val errorMsg = mapErrorCode(result.code, result.error)
                    _updateState.value = DeliveryUpdateState.Error(errorMsg)
                    updateErrorState(errorMsg)
                }
                is Resource.Loading -> _updateState.value = DeliveryUpdateState.Loading
            }
        }
    }

    /**
     * Confirm failed delivery
     */
    fun confirmFail(orderId: String) {
        viewModelScope.launch {
            _updateState.value = DeliveryUpdateState.Loading
            when (val result = updateDeliveryStatusUseCase.fail(orderId)) {
                is Resource.Success -> {
                    android.util.Log.d("OrderViewModel", "✅ Delivery fail confirmed, removing order...")
                    removeOrder(orderId)  // ✅ Remove immediately
                    _updateState.value = DeliveryUpdateState.Success
                    _uiState.value = _uiState.value.copy(
                        order = null,
                        canShowActions = false,
                        successMessage = null
                    )
                }
                is Resource.Error -> {
                    val errorMsg = mapErrorCode(result.code, result.error)
                    _updateState.value = DeliveryUpdateState.Error(errorMsg)
                    updateErrorState(errorMsg)
                }
                is Resource.Loading -> _updateState.value = DeliveryUpdateState.Loading
            }
        }
    }

    /**
     * Map HTTP error codes to user-friendly messages
     */
    private fun mapErrorCode(code: Int?, message: String): String {
        return when (code) {
            401 -> "Phiên đăng nhập hết hạn - Vui lòng đăng nhập lại"
            403 -> "Bạn không có quyền thực hiện hành động này"
            404 -> "Đơn hàng không tồn tại"
            500 -> "Lỗi máy chủ - Vui lòng thử lại sau"
            else -> message
        }
    }

    private fun updateErrorState(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message,
            canShowActions = false
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun resetUpdateState() {
        _updateState.value = DeliveryUpdateState.Idle
    }

    // ✅ Xóa order khỏi danh sách
    fun removeOrder(orderId: String) {
        android.util.Log.d("OrderViewModel", "🗑️  removeOrder() called for: $orderId")
        android.util.Log.d("OrderViewModel", "📊 Before: ${_uiState.value.orders.size} orders")

        val updatedOrders = _uiState.value.orders.filter { it.id != orderId }

        android.util.Log.d("OrderViewModel", "📊 After: ${updatedOrders.size} orders")
        android.util.Log.d("OrderViewModel", "🔍 Filtered out: ${_uiState.value.orders.size - updatedOrders.size} order(s)")

        _uiState.value = _uiState.value.copy(orders = updatedOrders)

        android.util.Log.d("OrderViewModel", "✅ State updated. New list: ${_uiState.value.orders.map { it.id }}")
    }
}
