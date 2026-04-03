package com.app.szone.data.repository

import com.app.szone.data.local.AuthDataStore
import com.app.szone.data.local.dao.UserDao
import com.app.szone.data.local.dao.WarehouseDao
import com.app.szone.data.local.entity.UserEntity
import com.app.szone.data.mapping.toEntity
import com.app.szone.data.mapping.toDomain
import com.app.szone.data.model.LoginRequestDto
import com.app.szone.data.remote.AuthService
import com.app.szone.domain.model.AuthResponse
import com.app.szone.domain.model.LoginRequest
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.User
import com.app.szone.domain.model.UserRole
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
            android.util.Log.d("AuthRepo", "🔐 LOGIN ATTEMPT: email=${request.email}")

            val dto = LoginRequestDto(request.email, request.password)
            val response = authService.login(dto)

            android.util.Log.d("AuthRepo", "📡 API RESPONSE: success=${response.success}, code=${response.code}")

            if (response.success && response.data != null) {
                val authData = response.data
                val user = authData.user

                // Validate fullName
                if (user.fullName.isNullOrBlank()) {
                    android.util.Log.e("AuthRepo", "❌ API ERROR: fullName is null or blank!")
                    return Resource.Error("API error: fullName is missing", 500)
                }

                val role = UserRole.fromRaw(user.roleName)

                android.util.Log.d("AuthRepo", "✅ API Response Data:")
                android.util.Log.d("AuthRepo", "  - fullName: '${user.fullName}' (length=${user.fullName.length})")
                android.util.Log.d("AuthRepo", "  - email: ${user.email}")
                android.util.Log.d("AuthRepo", "  - phone: ${user.phoneNumber}")
                android.util.Log.d("AuthRepo", "  - roleName: ${user.roleName}")
                android.util.Log.d("AuthRepo", "  - parsed role: $role")

                if (role == UserRole.UNKNOWN) {
                    android.util.Log.e("AuthRepo", "❌ UNKNOWN ROLE: ${user.roleName}")
                    return Resource.Error("Không có quyền truy cập", 403)
                }

                // Create UserEntity from DTO (with explicit mapping)
                val userEntity = UserEntity(
                    id = user.id,
                    email = user.email,
                    fullName = user.fullName,  // Explicitly set from API response
                    phone = user.phoneNumber,
                    roleName = user.roleName,
                    avatar = user.avatar,
                    status = user.status,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt
                )

                android.util.Log.d("AuthRepo", "✅ UserEntity created:")
                android.util.Log.d("AuthRepo", "  - fullName: '${userEntity.fullName}' (length=${userEntity.fullName.length})")

                // Clear old user data first to avoid stale data
                userDao.clearUsers()
                android.util.Log.d("AuthRepo", "🗑️  Cleared old user data")

                // Save to database
                userDao.insertUser(userEntity)
                android.util.Log.d("AuthRepo", "💾 User SAVED to database")

                // Verify saved (read back immediately)
                val savedUser = userDao.getUser()
                if (savedUser != null) {
                    android.util.Log.d("AuthRepo", "✅ VERIFICATION - Read from DB:")
                    android.util.Log.d("AuthRepo", "  - fullName: '${savedUser.fullName}' (length=${savedUser.fullName.length})")
                    android.util.Log.d("AuthRepo", "  - email: ${savedUser.email}")
                } else {
                    android.util.Log.e("AuthRepo", "❌ VERIFICATION FAILED - User not found in DB!")
                }

                // Save tokens
                authDataStore.saveTokens(authData.accessToken, authData.refreshToken)
                android.util.Log.d("AuthRepo", "💾 Tokens saved to DataStore")

                // Return success
                val authResponse = AuthResponse(
                    accessToken = authData.accessToken,
                    refreshToken = authData.refreshToken,
                    user = authData.user.toDomain()
                )
                android.util.Log.d("AuthRepo", "✅ LOGIN COMPLETE - Returning AuthResponse")
                android.util.Log.d("AuthRepo", "  - response.user.fullName: '${authResponse.user.fullName}'")
                Resource.Success(authResponse)
            } else {
                android.util.Log.e("AuthRepo", "❌ LOGIN FAILED: ${response.message}")
                Resource.Error(response.message ?: "Login failed", response.code)
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "❌ EXCEPTION: ${e.message}", e)
            e.printStackTrace()
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
            android.util.Log.d("AuthRepo", "📖 getCurrentUser() called")
            val userEntity = userDao.getUser()

            if (userEntity != null) {
                android.util.Log.d("AuthRepo", "✅ Found user in DB:")
                android.util.Log.d("AuthRepo", "  - id: ${userEntity.id}")
                android.util.Log.d("AuthRepo", "  - fullName: '${userEntity.fullName}'")
                android.util.Log.d("AuthRepo", "  - email: ${userEntity.email}")
                android.util.Log.d("AuthRepo", "  - phone: ${userEntity.phone}")
                android.util.Log.d("AuthRepo", "  - roleName: ${userEntity.roleName}")

                val user = userEntity.toDomain()
                android.util.Log.d("AuthRepo", "✅ Converted to domain model:")
                android.util.Log.d("AuthRepo", "  - fullName: '${user.fullName}'")

                Resource.Success(user)
            } else {
                android.util.Log.e("AuthRepo", "❌ User not found in DB")
                Resource.Error("User not found", 404)
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "❌ EXCEPTION in getCurrentUser: ${e.message}", e)
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
