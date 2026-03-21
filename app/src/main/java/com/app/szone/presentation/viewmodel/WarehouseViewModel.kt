package com.app.szone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.WarehouseModel
import com.app.szone.domain.usecase.GetCachedWarehouseUseCase
import com.app.szone.domain.usecase.GetCurrentUserUseCase
import com.app.szone.domain.usecase.GetWarehouseInfoUseCase
import com.app.szone.domain.usecase.ScanOrderArrivedUseCase
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
    val message: String? = null
)

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

    fun loadCachedWarehouse() {
        viewModelScope.launch {
            val cached = getCachedWarehouseUseCase()
            _uiState.value = _uiState.value.copy(warehouse = cached)
        }
    }

    fun loadWarehouseInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
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
                                message = null
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                userName = displayName,
                                message = warehouseResult.error
                            )
                        }
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, message = userResult.error)
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun scanOrder(orderId: String) {
        if (orderId.isBlank()) {
            _actionState.value = WarehouseActionState.Error("Mã đơn không hợp lệ")
            return
        }
        viewModelScope.launch {
            _actionState.value = WarehouseActionState.Loading
            when (val result = scanOrderArrivedUseCase(orderId)) {
                is Resource.Success -> {
                    val updated = listOf(orderId) + _uiState.value.scannedOrders
                    _uiState.value = _uiState.value.copy(scannedOrders = updated.distinct().take(20))
                    _actionState.value = WarehouseActionState.Success("Quét thành công")
                }
                is Resource.Error -> {
                    _actionState.value = WarehouseActionState.Error(result.error)
                }
                is Resource.Loading -> {
                    _actionState.value = WarehouseActionState.Loading
                }
            }
        }
    }

    fun resetActionState() {
        _actionState.value = WarehouseActionState.Idle
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
