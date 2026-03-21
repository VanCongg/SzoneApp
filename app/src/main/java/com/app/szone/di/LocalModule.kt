package com.app.szone.di

import androidx.room.Room
import com.app.szone.data.local.AppDatabase
import com.app.szone.data.local.AuthDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localDataModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "szone_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().warehouseDao() }
    single { get<AppDatabase>().orderDao() }
    single { get<AppDatabase>().pendingDeliveryActionDao() }

    single { AuthDataStore(androidContext()) }
}
