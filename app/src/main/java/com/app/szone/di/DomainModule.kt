package com.app.szone.di

import com.app.szone.domain.usecase.GetCachedWarehouseUseCase
import com.app.szone.domain.usecase.GetWarehouseInfoUseCase
import com.app.szone.domain.usecase.ScanOrderArrivedUseCase
import com.app.szone.domain.usecase.UpdateDeliveryStatusUseCase
import org.koin.dsl.module

val domainModule = module {
    single { UpdateDeliveryStatusUseCase(get()) }
    single { GetWarehouseInfoUseCase(get()) }
    single { ScanOrderArrivedUseCase(get()) }
    single { GetCachedWarehouseUseCase(get()) }
}
