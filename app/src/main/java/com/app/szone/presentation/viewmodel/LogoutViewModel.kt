package com.app.szone.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.szone.domain.model.Resource
import com.app.szone.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LogoutUiState {
    object Idle : LogoutUiState()
    object Loading : LogoutUiState()
    object Success : LogoutUiState()
    data class Error(val message: String) : LogoutUiState()
}

class LogoutViewModel(private val logoutUseCase: LogoutUseCase) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LogoutUiState>(LogoutUiState.Idle)
    val uiState: StateFlow<LogoutUiState> = _uiState.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _uiState.value = LogoutUiState.Loading

            val result = logoutUseCase()
            
            _uiState.value = when (result) {
                is Resource.Success -> LogoutUiState.Success
                is Resource.Error -> LogoutUiState.Error(result.error)
                is Resource.Loading -> LogoutUiState.Loading
            }
        }
    }

    fun resetState() {
        _uiState.value = LogoutUiState.Idle
    }
}

