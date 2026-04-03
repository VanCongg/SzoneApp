package com.app.szone.data.model

/**
 * Custom exception for API errors with detailed information
 * Supports HTTP status codes and error categorization
 */
sealed class ApiException(override val message: String) : Exception(message) {
    data class HttpException(val code: Int, override val message: String) : ApiException(message)
    data class NetworkException(override val message: String) : ApiException(message)
    data class UnknownException(override val message: String, val throwable: Throwable? = null) : ApiException(message)
    data class ClientException(override val message: String, val code: Int? = null) : ApiException(message)
    data class ServerException(override val message: String, val code: Int? = null) : ApiException(message)

    companion object {
        fun from(throwable: Throwable, code: Int? = null): ApiException {
            return when {
                code != null && code in 400..499 -> ClientException(
                    message = throwable.message ?: "Client error",
                    code = code
                )
                code != null && code in 500..599 -> ServerException(
                    message = throwable.message ?: "Server error",
                    code = code
                )
                else -> UnknownException(
                    message = throwable.message ?: "Unknown error occurred",
                    throwable = throwable
                )
            }
        }

        fun unauthorized() = HttpException(401, "Unauthorized - Please login again")
        fun forbidden() = HttpException(403, "Access denied - No permission")
        fun notFound() = HttpException(404, "Resource not found")
        fun serverError() = HttpException(500, "Server error - Please try again later")
        fun networkError() = NetworkException("Network error - Check your connection")
    }
}

