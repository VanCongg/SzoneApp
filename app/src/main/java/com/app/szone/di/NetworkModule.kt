package com.app.szone.di

import com.app.szone.BuildConfig
import com.app.szone.data.remote.AuthInterceptor
import com.app.szone.data.remote.AuthService
import com.app.szone.data.remote.OrderService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true  // ✅ Chấp nhận null/kiểu không khớp
            isLenient = true  // ✅ Linh hoạt với JSON malformed
        }

        // Add HTTP logging interceptor for debugging
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            android.util.Log.d("HttpLogging", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(get()))
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single<AuthService> { get<Retrofit>().create(AuthService::class.java) }
    single<OrderService> { get<Retrofit>().create(OrderService::class.java) }
}
