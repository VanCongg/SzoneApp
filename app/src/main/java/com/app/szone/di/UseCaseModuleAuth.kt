package com.app.szone.di

import com.app.szone.domain.usecase.GetCurrentTokenUseCase
import com.app.szone.domain.usecase.GetCurrentUserUseCase
import com.app.szone.domain.usecase.IsLoggedInUseCase
import com.app.szone.domain.usecase.LoginUseCase
import com.app.szone.domain.usecase.LogoutUseCase
import org.koin.dsl.module

val authUseCaseModule = module {
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get()) }
    single { GetCurrentUserUseCase(get()) }
    single { IsLoggedInUseCase(get()) }
    single { GetCurrentTokenUseCase(get()) }
}
