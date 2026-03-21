package com.app.szone.di

import com.app.szone.domain.usecase.ConfirmDeliveryFailUseCase
import com.app.szone.domain.usecase.ConfirmDeliverySuccessUseCase
import com.app.szone.domain.usecase.GetOrderDetailsUseCase
import com.app.szone.domain.usecase.GetStartScreenUseCase
import com.app.szone.domain.usecase.SyncPendingDeliveryActionsUseCase
import org.koin.dsl.module

val coreUseCaseModule = module {
    single { GetStartScreenUseCase(welcomeRepository = get()) }
    single { GetOrderDetailsUseCase(get()) }
    single { ConfirmDeliverySuccessUseCase(get()) }
    single { ConfirmDeliveryFailUseCase(get()) }
    single { SyncPendingDeliveryActionsUseCase(get()) }
}
