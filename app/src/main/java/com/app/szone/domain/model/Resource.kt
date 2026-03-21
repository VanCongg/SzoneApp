package com.app.szone.domain.model

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val error: String, val code: Int? = null) : Resource<T>()
    object Loading : Resource<Nothing>()
}

