package com.app.szone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.UserRole
import com.app.szone.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(
        val message: String,
        val userRole: UserRole,
        val fullName: String = "",
        val email: String = "",
        val phoneNumber: String = ""
    ) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel for Login Screen
 * Handles user authentication and validation
 * Implements MVVM pattern with comprehensive error handling
 */
class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Perform login with email and password
     * Validates input before API call
     * Maps error codes to user-friendly messages
     */
    fun login(email: String, password: String) {
        if (!validateInput(email, password)) {
            android.util.Log.w("LoginVM", "❌ VALIDATION FAILED: email=$email")
            _uiState.value = LoginUiState.Error("Email và mật khẩu không được trống")
            return
        }

        android.util.Log.d("LoginVM", "🔐 LOGIN STARTED: email=$email")
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val result = loginUseCase(email, password)
            android.util.Log.d("LoginVM", "📡 LOGIN RESULT: $result")

            _uiState.value = when (result) {
                is Resource.Success -> {
                    val userRole = UserRole.fromRaw(result.data.user.roleName)
                    android.util.Log.d("LoginVM", "✅ LOGIN SUCCESS: role=$userRole, user=${result.data.user.fullName}")

                    when (userRole) {
                        UserRole.SHIPPER,
                        UserRole.WAREHOUSE_SCANNER -> {
                            // Extract user data from response
                            val user = result.data.user
                            android.util.Log.d("LoginVM", "✅ ROLE VALID: $userRole, fullName=${user.fullName}")
                            LoginUiState.Success(
                                message = "Đăng nhập thành công",
                                userRole = userRole,
                                fullName = user.fullName,
                                email = user.email,
                                phoneNumber = user.phone
                            )
                        }
                        UserRole.UNKNOWN -> {
                            android.util.Log.e("LoginVM", "❌ UNKNOWN ROLE: ${result.data.user.roleName}")
                            LoginUiState.Error("Không có quyền truy cập ứng dụng này")
                        }
                    }
                }
                is Resource.Error -> {
                    val errorMessage = mapErrorCode(result.code, result.error)
                    android.util.Log.e("LoginVM", "❌ LOGIN ERROR: code=${result.code}, message=${result.error}, mapped=$errorMessage")
                    LoginUiState.Error(errorMessage)
                }
                is Resource.Loading -> LoginUiState.Loading
            }
        }
    }

    /**
     * Validate email and password format
     */
    private fun validateInput(email: String, password: String): Boolean {
        return email.isNotBlank() && 
               password.isNotBlank() && 
               email.contains("@") && 
               email.contains(".")
    }

    /**
     * Map HTTP error codes to user-friendly Vietnamese messages
     */
    private fun mapErrorCode(code: Int?, message: String): String {
        return when (code) {
            401 -> "Email hoặc mật khẩu không chính xác"
            403 -> "Tài khoản không có quyền truy cập"
            404 -> "Tài khoản không tồn tại"
            500 -> "Lỗi máy chủ - Vui lòng thử lại sau"
            else -> message
        }
    }

    /**
     * Reset UI state for new login attempt
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

