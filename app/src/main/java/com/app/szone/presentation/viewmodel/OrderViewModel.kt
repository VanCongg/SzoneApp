package com.app.szone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.Resource
import com.app.szone.domain.usecase.GetOrderDetailsUseCase
import com.app.szone.domain.usecase.UpdateDeliveryStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DeliveryUpdateState {
    object Idle : DeliveryUpdateState()
    object Loading : DeliveryUpdateState()
    data class Success(val message: String) : DeliveryUpdateState()
    data class Error(val message: String) : DeliveryUpdateState()
}

data class OrderUiState(
    val isLoading: Boolean = false,
    val order: OrderModel? = null,
    val message: String? = null,
    val canShowActions: Boolean = false
)

class OrderViewModel(
    private val getOrderDetailsUseCase: GetOrderDetailsUseCase,
    private val updateDeliveryStatusUseCase: UpdateDeliveryStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val _updateState = MutableStateFlow<DeliveryUpdateState>(DeliveryUpdateState.Idle)
    val updateState: StateFlow<DeliveryUpdateState> = _updateState.asStateFlow()

    fun loadOrder(orderId: String, shipperName: String, shipperPhone: String) {
        if (orderId.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "QR không hợp lệ")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            when (val result = getOrderDetailsUseCase(orderId, shipperName, shipperPhone)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        order = result.data,
                        canShowActions = true,
                        message = null
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = result.error,
                        canShowActions = false
                    )
                }

                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun confirmSuccess(orderId: String, shopId: String) {
        viewModelScope.launch {
            _updateState.value = DeliveryUpdateState.Loading
            when (val result = updateDeliveryStatusUseCase.success(orderId, shopId)) {
                is Resource.Success -> _updateState.value = DeliveryUpdateState.Success("Giao hàng thành công")
                is Resource.Error -> _updateState.value = DeliveryUpdateState.Error(result.error)
                is Resource.Loading -> _updateState.value = DeliveryUpdateState.Loading
            }
        }
    }

    fun confirmFail(orderId: String) {
        viewModelScope.launch {
            _updateState.value = DeliveryUpdateState.Loading
            when (val result = updateDeliveryStatusUseCase.fail(orderId)) {
                is Resource.Success -> _updateState.value = DeliveryUpdateState.Success("Đã cập nhật giao thất bại")
                is Resource.Error -> _updateState.value = DeliveryUpdateState.Error(result.error)
                is Resource.Loading -> _updateState.value = DeliveryUpdateState.Loading
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun resetUpdateState() {
        _updateState.value = DeliveryUpdateState.Idle
    }
}
