/**
 * MVVM + Clean Architecture - Template for New Features
 * Copy này template khi thêm feature mới
 */

// ============================================================
// 1. DOMAIN LAYER - Business Model
// ============================================================
package com.app.szone.domain.model

data class OrderModel(
    val id: String,
    val status: String,
    val totalPrice: Double,
    val recipientName: String,
    val recipientPhone: String,
    val deliveryAddress: String
)

// ============================================================
// 2. DOMAIN LAYER - Repository Interface
// ============================================================
package com.app.szone.domain.repository

interface OrderRepository {
    suspend fun getOrderDetails(orderId: String): Resource<OrderModel>
    suspend fun updateDeliveryStatus(orderId: String, status: String): Resource<Unit>
}

// ============================================================
// 3. DOMAIN LAYER - Use Case
// ============================================================
package com.app.szone.domain.usecase

class GetOrderDetailsUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String): Resource<OrderModel> {
        if (orderId.isBlank()) {
            return Resource.Error("Invalid order ID")
        }
        return repository.getOrderDetails(orderId)
    }
}

class UpdateOrderStatusUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: String, status: String): Resource<Unit> {
        return repository.updateOrderStatus(orderId, status)
    }
}

// ============================================================
// 4. DATA LAYER - DTO Models
// ============================================================
package com.app.szone.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String,
    val status: String,
    val totalPrice: Double,
    val recipient: RecipientDto,
    val address: String
)

@Serializable
data class RecipientDto(
    val name: String,
    val phone: String
)

// ============================================================
// 5. DATA LAYER - Retrofit Service
// ============================================================
package com.app.szone.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface OrderService {
    @GET("api/v1/orders/{orderId}")
    suspend fun getOrderDetails(
        @Path("orderId") orderId: String
    ): ApiResponse<OrderDto>
}

// ============================================================
// 6. DATA LAYER - Room Entity & DAO
// ============================================================
package com.app.szone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val status: String,
    val totalPrice: Double,
    val recipientName: String,
    val recipientPhone: String,
    val deliveryAddress: String,
    val createdAt: Long = System.currentTimeMillis()
)

package com.app.szone.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): OrderEntity?

    @Query("SELECT * FROM orders")
    suspend fun getAllOrders(): List<OrderEntity>

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrder(id: String)
}

// ============================================================
// 7. DATA LAYER - Mapper
// ============================================================
package com.app.szone.data.mapping

import com.app.szone.data.local.entity.OrderEntity
import com.app.szone.data.model.OrderDto
import com.app.szone.domain.model.OrderModel

// DTO → Entity
fun OrderDto.toEntity() = OrderEntity(
    id = id,
    status = status,
    totalPrice = totalPrice,
    recipientName = recipient.name,
    recipientPhone = recipient.phone,
    deliveryAddress = address
)

// Entity → Domain Model
fun OrderEntity.toDomain() = OrderModel(
    id = id,
    status = status,
    totalPrice = totalPrice,
    recipientName = recipientName,
    recipientPhone = recipientPhone,
    deliveryAddress = deliveryAddress
)

// DTO → Domain Model (direct)
fun OrderDto.toDomain() = OrderModel(
    id = id,
    status = status,
    totalPrice = totalPrice,
    recipientName = recipient.name,
    recipientPhone = recipient.phone,
    deliveryAddress = address
)

// ============================================================
// 8. DATA LAYER - Repository Implementation
// ============================================================
package com.app.szone.data.repository

class OrderRepositoryImpl(
    private val orderService: OrderService,
    private val orderDao: OrderDao
) : OrderRepository {

    override suspend fun getOrderDetails(orderId: String): Resource<OrderModel> {
        return try {
            // Try API first
            val response = orderService.getOrderDetails(orderId)

            if (response.success && response.data != null) {
                // Save to local DB
                orderDao.insertOrder(response.data.toEntity())

                // Return domain model
                Resource.Success(response.data.toDomain())
            } else {
                // Try cached data
                val cached = orderDao.getOrderById(orderId)
                if (cached != null) {
                    Resource.Success(cached.toDomain())
                } else {
                    Resource.Error(response.message ?: "Failed to load order", response.code)
                }
            }
        } catch (e: Exception) {
            // Try cached data on error
            try {
                val cached = orderDao.getOrderById(orderId)
                if (cached != null) {
                    Resource.Success(cached.toDomain())
                } else {
                    Resource.Error(e.message ?: "Network error", null)
                }
            } catch (ex: Exception) {
                Resource.Error(ex.message ?: "Unknown error", null)
            }
        }
    }

    override suspend fun updateDeliveryStatus(orderId: String, status: String): Resource<Unit> {
        return try {
            val request = UpdateOrderStatusRequest(status)
            val response = orderService.updateOrderStatus(orderId, request)

            if (response.success) {
                // Update local DB
                val order = orderDao.getOrderById(orderId)
                if (order != null) {
                    orderDao.updateOrder(order.copy(status = status))
                }
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message ?: "Failed to update", response.code)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error", null)
        }
    }
}

// ============================================================
// 9. PRESENTATION LAYER - UI State
// ============================================================
package com.app.szone.presentation.state

sealed class OrderListState {
    object Idle : OrderListState()
    object Loading : OrderListState()
    data class Success(val orders: List<OrderModel>) : OrderListState()
    data class Error(val message: String) : OrderListState()
}

sealed class OrderDetailState {
    object Idle : OrderDetailState()
    object Loading : OrderDetailState()
    data class Success(val order: OrderModel) : OrderDetailState()
    data class Error(val message: String) : OrderDetailState()
}

data class OrderScreenUiState(
    val isLoading: Boolean = false,
    val order: OrderModel? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

// ============================================================
// 10. PRESENTATION LAYER - ViewModel
// ============================================================
package com.app.szone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.szone.domain.model.OrderModel
import com.app.szone.domain.model.Resource
import com.app.szone.domain.usecase.GetOrderDetailsUseCase
import com.app.szone.domain.usecase.UpdateOrderStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrderUiState(
    val isLoading: Boolean = false,
    val order: OrderModel? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for Order Management
 * Follows MVVM + Clean Architecture pattern
 */
class OrderViewModel(
    private val getOrderDetailsUseCase: GetOrderDetailsUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    fun loadOrder(orderId: String) {
        if (orderId.isBlank()) {
            updateErrorState("Invalid order ID")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = getOrderDetailsUseCase(orderId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        order = result.data,
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    updateErrorState(mapErrorCode(result.code, result.error))
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun updateStatus(orderId: String, status: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = updateOrderStatusUseCase(orderId, status)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Order updated successfully"
                    )
                    // Reload order
                    loadOrder(orderId)
                }
                is Resource.Error -> {
                    updateErrorState(mapErrorCode(result.code, result.error))
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun mapErrorCode(code: Int?, message: String): String {
        return when (code) {
            401 -> "Session expired - Please login again"
            403 -> "You don't have permission"
            404 -> "Order not found"
            500 -> "Server error - Try again later"
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
}

// ============================================================
// 11. PRESENTATION LAYER - Compose Screen
// ============================================================
package com.app.szone.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.szone.presentation.viewmodel.OrderViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OrderDetailScreen(
    orderId: String,
    viewModel: OrderViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
            uiState.order != null -> {
                val order = uiState.order!!
                Text("Order: ${order.id}")
                Text("Status: ${order.status}")
                Text("Total: ${order.totalPrice}")

                Button(onClick = { viewModel.updateStatus(order.id, "COMPLETED") }) {
                    Text("Mark as Completed")
                }
            }
        }
    }
}

// ============================================================
// 12. DI MODULE - Add Use Cases & Repositories
// ============================================================
package com.app.szone.di

val orderModule = module {
    // Services
    single<OrderService> { get<Retrofit>().create(OrderService::class.java) }

    // DAOs
    single { get<AppDatabase>().orderDao() }

    // Repositories
    single<OrderRepository> {
        OrderRepositoryImpl(
            orderService = get(),
            orderDao = get()
        )
    }

    // Use Cases
    single { GetOrderDetailsUseCase(get()) }
    single { UpdateOrderStatusUseCase(get()) }

    // ViewModels
    viewModel { OrderViewModel(get(), get()) }
}

// ============================================================
// QUICK CHECKLIST
// ============================================================

/*
✅ Steps to add new feature:

1. Create Domain Models
   - OrderModel data class

2. Create Repository Interface
   - OrderRepository interface

3. Create Use Cases
   - GetOrderDetailsUseCase
   - UpdateOrderStatusUseCase

4. Create Data Layer
   - OrderDto (serializable)
   - OrderEntity (room)
   - OrderDao (DAO)
   - OrderService (Retrofit)
   - OrderRepositoryImpl (implement interface)

5. Create Mappers
   - OrderDto → OrderEntity
   - OrderEntity → OrderModel

6. Create UI States
   - OrderListState
   - OrderDetailState
   - OrderUiState

7. Create ViewModel
   - Manage states with StateFlow
   - Map error codes
   - Call use cases

8. Create Compose UI
   - Collect StateFlow
   - Show loading/error/success states
   - Call ViewModel functions

9. Register in DI
   - Add to relevant module
   - Register UseCase, Repository, ViewModel

Done! 🎉
*/

