package com.app.szone.data.repository

import com.app.szone.data.local.AuthDataStore
import com.app.szone.data.local.dao.UserDao
import com.app.szone.data.local.dao.WarehouseDao
import com.app.szone.data.mapping.toEntity
import com.app.szone.data.mapping.toDomain
import com.app.szone.data.model.LoginRequestDto
import com.app.szone.data.remote.AuthService
import com.app.szone.domain.model.AuthResponse
import com.app.szone.domain.model.LoginRequest
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.User
import com.app.szone.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val authDataStore: AuthDataStore,
    private val userDao: UserDao,
    private val warehouseDao: WarehouseDao
) : AuthRepository {
    
    override suspend fun login(request: LoginRequest): Resource<AuthResponse> {
        return try {
            val dto = LoginRequestDto(request.email, request.password)
            val response = authService.login(dto)

            if (response.success && response.data != null) {
                val authData = response.data

                // Lưu token
                authDataStore.saveTokens(authData.accessToken, authData.refreshToken)
                
                // Lưu user vào database
                userDao.insertUser(authData.user.toEntity())
                
                // Convert to domain model
                val authResponse = AuthResponse(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken,
                    user = authData.user.toDomain()
                )
                Resource.Success(authResponse)
            } else {
                Resource.Error(response.message ?: "Login failed", response.code)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred", null)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            // Lấy token hiện tại từ DataStore
            val token = authDataStore.accessTokenFlow.first().orEmpty()
            
            // Gọi API logout
            val response = authService.logout("Bearer $token")

            if (response.success) {
                // Xóa token
                authDataStore.clearTokens()
                // Xóa user từ database
                userDao.clearUsers()
                // Xóa dữ liệu kho
                warehouseDao.clearWarehouse()
                Resource.Success(Unit)
            } else {
                Resource.Error(response.message ?: "Logout failed", response.code)
            }
        } catch (e: Exception) {
            // Xóa dù có lỗi API
            authDataStore.clearTokens()
            userDao.clearUsers()
            warehouseDao.clearWarehouse()
            Resource.Error(e.message ?: "Unknown error occurred", null)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val userEntity = userDao.getUser()
            if (userEntity != null) {
                Resource.Success(userEntity.toDomain())
            } else {
                Resource.Error("User not found", 404)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error occurred", null)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return authDataStore.accessTokenFlow.map { token ->
            !token.isNullOrEmpty()
        }
    }

    override fun getCurrentToken(): Flow<String?> {
        return authDataStore.accessTokenFlow
    }
}
