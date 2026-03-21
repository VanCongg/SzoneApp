package com.app.szone.di

import com.app.szone.data.local.WelcomeRepositoryImpl
import com.app.szone.data.repository.AuthRepositoryImpl
import com.app.szone.data.repository.OrderRepositoryImpl
import com.app.szone.data.repository.WarehouseRepositoryImpl
import com.app.szone.domain.repository.AuthRepository
import com.app.szone.domain.repository.OrderRepository
import com.app.szone.domain.repository.WarehouseRepository
import com.app.szone.domain.repository.WelcomeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<WelcomeRepository> {
        WelcomeRepositoryImpl(androidContext())
    }

    single<AuthRepository> {
        AuthRepositoryImpl(
            authService = get(),
            authDataStore = get(),
            userDao = get(),
            warehouseDao = get()
        )
    }

    single<OrderRepository> {
        OrderRepositoryImpl(
            orderService = get(),
            orderDao = get(),
            pendingActionDao = get()
        )
    }

    single<WarehouseRepository> {
        WarehouseRepositoryImpl(
            orderService = get(),
            warehouseDao = get()
        )
    }
}
