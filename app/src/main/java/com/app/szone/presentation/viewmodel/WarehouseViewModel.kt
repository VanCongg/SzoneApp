package com.app.szone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.WarehouseModel
import com.app.szone.domain.usecase.GetCachedWarehouseUseCase
import com.app.szone.domain.usecase.GetCurrentUserUseCase
import com.app.szone.domain.usecase.GetWarehouseInfoUseCase
import com.app.szone.domain.usecase.ScanOrderArrivedUseCase
import com.app.szone.presentation.state.WarehouseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WarehouseActionState {
    object Idle : WarehouseActionState()
    object Loading : WarehouseActionState()
    data class Success(val message: String) : WarehouseActionState()
    data class Error(val message: String) : WarehouseActionState()
}

data class WarehouseUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val warehouse: WarehouseModel? = null,
    val scannedOrders: List<String> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for Warehouse/Scanner operations
 * Manages warehouse info, QR scanning, and order arrival processing
 * Implements MVVM with comprehensive error handling
 */
class WarehouseViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getWarehouseInfoUseCase: GetWarehouseInfoUseCase,
    private val scanOrderArrivedUseCase: ScanOrderArrivedUseCase,
    private val getCachedWarehouseUseCase: GetCachedWarehouseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WarehouseUiState())
    val uiState: StateFlow<WarehouseUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<WarehouseActionState>(WarehouseActionState.Idle)
    val actionState: StateFlow<WarehouseActionState> = _actionState.asStateFlow()

    /**
     * Load cached warehouse info from local database
     * Used for faster UI display
     */
    fun loadCachedWarehouse() {
        viewModelScope.launch {
            val cached = getCachedWarehouseUseCase()
            _uiState.value = _uiState.value.copy(warehouse = cached)
        }
    }

    /**
     * Load warehouse info from API
     * Fetches scanner info and warehouse details
     */
    fun loadWarehouseInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val userResult = getCurrentUserUseCase()) {
                is Resource.Success -> {
                    val scannerId = userResult.data.id
                    val displayName = userResult.data.fullName

                    when (val warehouseResult = getWarehouseInfoUseCase(scannerId)) {
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                userName = displayName,
                                warehouse = warehouseResult.data,
                                errorMessage = null
                            )
                        }
                        is Resource.Error -> {
                            updateErrorState(warehouseResult.error)
                        }
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
                is Resource.Error -> {
                    updateErrorState(userResult.error)
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    /**
     * Scan order and update arrival status
     * Validates QR code and processes order arrival
     */
    fun scanOrderArrived(orderId: String) {
        if (orderId.isBlank()) {
            updateErrorState("QR code không hợp lệ")
            return
        }

        viewModelScope.launch {
            _actionState.value = WarehouseActionState.Loading

            val warehouse = _uiState.value.warehouse
            if (warehouse == null) {
                _actionState.value = WarehouseActionState.Error("Thông tin kho không được tải")
                return@launch
            }

            when (val result = scanOrderArrivedUseCase(orderId, warehouse.name, warehouse.address)) {
                is Resource.Success -> {
                    android.util.Log.d("WarehouseVM", "✅ Scan order arrived success: orderId=$orderId")
                    _actionState.value = WarehouseActionState.Success("✅ Quét hàng thành công")
                    _uiState.value = _uiState.value.copy(
                        scannedOrders = _uiState.value.scannedOrders + orderId,
                        successMessage = "Đã ghi nhận đơn hàng #$orderId"
                    )
                }
                is Resource.Error -> {
                    val errorMsg = mapErrorCode(result.code, result.error)
                    android.util.Log.e("WarehouseVM", "❌ Scan error: $errorMsg")
                    _actionState.value = WarehouseActionState.Error(errorMsg)
                    updateErrorState(errorMsg)
                }
                is Resource.Loading -> {
                    _actionState.value = WarehouseActionState.Loading
                }
            }
        }
    }

    /**
     * Map HTTP error codes to user-friendly messages
     */
    private fun mapErrorCode(code: Int?, message: String): String {
        return when (code) {
            401 -> "Phiên đăng nhập hết hạn"
            403 -> "Bạn không có quyền thực hiện hành động này"
            404 -> "Đơn hàng không tồn tại"
            500 -> "Lỗi máy chủ - vui lòng thử lại"
            else -> message
        }
    }

    private fun updateErrorState(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun resetActionState() {
        _actionState.value = WarehouseActionState.Idle
    }
}
