package com.app.szone.data.remote

import android.util.Log
import com.app.szone.data.local.AuthDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

/**
 * Adds authentication token to all API requests
 * Logs network requests/responses for debugging
 */
class AuthInterceptor(private val authDataStore: AuthDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            authDataStore.accessTokenFlow.first()
        }
        
        val userId = runBlocking {
            authDataStore.userIdFlow.first()
        }

        val originalRequest = chain.request()

        // Log request details
        Log.d("AuthInterceptor", "→ ${originalRequest.method} ${originalRequest.url}")

        // Check if request has body
        val hasBody = originalRequest.body != null
        Log.d("AuthInterceptor", "  Request has body: $hasBody")
        if (hasBody && originalRequest.body != null) {
            try {
                val buffer = okio.Buffer()
                originalRequest.body!!.writeTo(buffer)
                Log.d("AuthInterceptor", "  Body: ${buffer.readUtf8()}")
            } catch (e: Exception) {
                Log.d("AuthInterceptor", "  Body: (unable to read)")
            }
        }

        Log.d("AuthInterceptor", "  Token available: ${token != null}")
        if (token != null) {
            Log.d("AuthInterceptor", "  Token: ${token.take(20)}...${token.takeLast(10)}")
        }
        
        Log.d("AuthInterceptor", "  UserId available: ${userId != null} (value: $userId)")

        val newRequest = if (token != null) {
            val builder = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
            
            // Add x-user-id header if available
            if (userId != null) {
                builder.header("x-user-id", userId)
            }
            
            builder.build()
        } else {
            Log.w("AuthInterceptor", "⚠️  NO TOKEN FOUND - Request will be made without authorization")
            originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .build()
        }

        return try {
            val response = chain.proceed(newRequest)
            Log.d("AuthInterceptor", "← ${response.code} ${response.message}")
            if (response.code >= 400) {
                try {
                    val bodyString = response.body?.string() ?: ""
                    Log.e("AuthInterceptor", "  Error response: $bodyString")
                    // Recreate response since body was consumed
                    response.newBuilder()
                        .body(okhttp3.ResponseBody.create(response.body?.contentType(), bodyString))
                        .build()
                } catch (e: Exception) {
                    response
                }
            } else {
                response
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "❌ Error: ${e.message}", e)
            throw e
        }
    }
}



