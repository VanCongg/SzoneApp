package com.app.szone.di

import com.app.szone.presentation.viewmodel.LoginViewModel
import com.app.szone.presentation.viewmodel.LogoutViewModel
import com.app.szone.presentation.viewmodel.OrderViewModel
import com.app.szone.presentation.viewmodel.WarehouseViewModel
import com.app.szone.presentation.viewmodel.CurrentUserViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { LogoutViewModel(get()) }
    viewModel { OrderViewModel(get(), get()) }
    viewModel { WarehouseViewModel(get(), get(), get(), get()) }
    viewModel { CurrentUserViewModel(get()) }
}
