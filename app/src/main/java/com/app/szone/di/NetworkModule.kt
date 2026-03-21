package com.app.szone.di

import com.app.szone.BuildConfig
import com.app.szone.data.remote.AuthInterceptor
import com.app.szone.data.remote.AuthService
import com.app.szone.data.remote.OrderService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single {
        val json = Json { ignoreUnknownKeys = true }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(get()))
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
