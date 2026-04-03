package com.app.szone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.szone.domain.model.Resource
import com.app.szone.domain.model.User
import com.app.szone.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CurrentUserUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null
)

class CurrentUserViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrentUserUiState())
    val uiState: StateFlow<CurrentUserUiState> = _uiState.asStateFlow()

    init {
        android.util.Log.d("CurrentUserVM", "🚀 INIT: CurrentUserViewModel created")
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            android.util.Log.d("CurrentUserVM", "🔄 START: loadCurrentUser()")
            android.util.Log.d("CurrentUserVM", "🔄 Timestamp: ${System.currentTimeMillis()}")

            val result = getCurrentUserUseCase()
            android.util.Log.d("CurrentUserVM", "📦 Result: $result")

            _uiState.value = when (result) {
                is Resource.Success -> {
                    val user = result.data
                    android.util.Log.d("CurrentUserVM", "✅ SUCCESS:")
                    android.util.Log.d("CurrentUserVM", "  - id: ${user.id}")
                    android.util.Log.d("CurrentUserVM", "  - fullName: '${user.fullName}'")
                    android.util.Log.d("CurrentUserVM", "  - email: ${user.email}")
                    android.util.Log.d("CurrentUserVM", "  - phone: ${user.phone}")
                    android.util.Log.d("CurrentUserVM", "  - roleName: ${user.roleName}")

                    _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    android.util.Log.e("CurrentUserVM", "❌ ERROR: ${result.error}")
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error
                    )
                }
                is Resource.Loading -> {
                    android.util.Log.d("CurrentUserVM", "⏳ LOADING...")
                    _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun refresh() {
        loadCurrentUser()
    }
}

