package com.app.szone.presentation.state

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

object AuthState {
    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _userName = mutableStateOf("Nguyễn Huy")
    val userName: State<String> = _userName

    private val _userEmail = mutableStateOf("nguyen.huy@example.com")
    val userEmail: State<String> = _userEmail

    fun login(name: String = "Nguyễn Huy", email: String = "nguyen.huy@example.com") {
        _userName.value = name
        _userEmail.value = email
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
        _userName.value = "Nguyễn Huy"
        _userEmail.value = "nguyen.huy@example.com"
    }

    fun isAuthenticated(): Boolean = _isLoggedIn.value
}

