package com.app.szone.data.remote

import android.util.Log
import com.app.szone.data.local.AuthDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds authentication token to all API requests
 * Logs network requests/responses for debugging
 */
class AuthInterceptor(private val authDataStore: AuthDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            authDataStore.accessTokenFlow.first()
        }

        val originalRequest = chain.request()

        // Log request
        Log.d("AuthInterceptor", "→ ${originalRequest.method} ${originalRequest.url}")

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .build()
        }

        return try {
            val response = chain.proceed(newRequest)
            Log.d("AuthInterceptor", "← ${response.code} ${response.message}")
            response
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error: ${e.message}", e)
            throw e
        }
    }
}



