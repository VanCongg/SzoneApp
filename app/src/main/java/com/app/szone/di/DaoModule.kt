//package com.app.szone.di
//
//import androidx.room.Room
//import org.koin.dsl.module
//import kotlin.jvm.java
//
//val daoModule = module {
//    single {
//        Room.databaseBuilder(
//            get(),
//            AppDatabase::class.java,
//            "vocago_database"
//        ).build()
//    }
//
//    single {
//        get<AppDatabase>().userDao()
//    }
//
//}
