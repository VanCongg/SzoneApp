package com.app.szone.presentation.state

/**
 * Unified UI State Management for all screens
 * Follows MVVM + Clean Architecture pattern
 */

// Base sealed class for all UI states
sealed class UIState<out T> {
    object Idle : UIState<Nothing>()
    object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val message: String, val code: Int? = null, val exception: Throwable? = null) : UIState<Nothing>()
}

// Extension functions for UIState handling
fun <T> UIState<T>.getDataOrNull(): T? = if (this is UIState.Success) this.data else null

fun <T> UIState<T>.isLoading(): Boolean = this is UIState.Loading

fun <T> UIState<T>.isSuccess(): Boolean = this is UIState.Success

fun <T> UIState<T>.isError(): Boolean = this is UIState.Error

fun <T, R> UIState<T>.map(transform: (T) -> R): UIState<R> = when (this) {
    is UIState.Success -> UIState.Success(transform(data))
    is UIState.Error -> this as UIState<R>
    is UIState.Loading -> UIState.Loading
    is UIState.Idle -> UIState.Idle
}

/**
 * Specific UI States for each screen
 */

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val userRole: String, val message: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class OrderListState {
    object Idle : OrderListState()
    object Loading : OrderListState()
    data class Success(val orders: List<Any>) : OrderListState()
    data class Error(val message: String) : OrderListState()
}

sealed class OrderDetailState {
    object Idle : OrderDetailState()
    object Loading : OrderDetailState()
    data class Success(val orderDetail: Any) : OrderDetailState()
    data class Error(val message: String) : OrderDetailState()
}

sealed class WarehouseState {
    object Idle : WarehouseState()
    object Loading : WarehouseState()
    data class Success(val warehouse: Any) : WarehouseState()
    data class Error(val message: String) : WarehouseState()
}

sealed class DeliveryUpdateState {
    object Idle : DeliveryUpdateState()
    object Loading : DeliveryUpdateState()
    object Success : DeliveryUpdateState()
    data class Error(val message: String) : DeliveryUpdateState()
}

