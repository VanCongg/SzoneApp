package com.app.szone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.szone.domain.model.Resource
import com.app.szone.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val message: String, val userRole: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (!validateInput(email, password)) {
            _uiState.value = LoginUiState.Error("Email và mật khẩu không được trống")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            val result = loginUseCase(email, password)
            
            _uiState.value = when (result) {
                is Resource.Success -> {
                    val userRole = result.data.user.roleName
                    
                    // Kiểm tra vai trò
                    when (userRole) {
                        "SHIPPER" -> LoginUiState.Success("Đăng nhập thành công", userRole)
                        "WAREHOUSE_SCANNER" -> LoginUiState.Success("Đăng nhập thành công", userRole)
                        else -> LoginUiState.Error("Không có quyền truy cập (Role: $userRole)")
                    }
                }
                is Resource.Error -> {
                    val errorMessage = when (result.code) {
                        401 -> "Email hoặc mật khẩu không chính xác"
                        403 -> "Tài khoản của bạn bị khóa"
                        500 -> "Lỗi máy chủ, vui lòng thử lại sau"
                        else -> result.error
                    }
                    LoginUiState.Error(errorMessage)
                }
                is Resource.Loading -> LoginUiState.Loading
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return email.isNotBlank() && 
               password.isNotBlank() && 
               email.contains("@")
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

