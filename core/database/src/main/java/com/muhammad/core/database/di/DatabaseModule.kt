package com.muhammad.core.database.di

import androidx.room.*
import com.muhammad.core.database.RemoteLocalRunDataSource
import com.muhammad.core.database.RunDatabase
import com.muhammad.core.domain.run.LocalRunDataSource
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.*

val databaseModule = module {
    single {
        Room.databaseBuilder(androidApplication(), RunDatabase::class.java, "run.db").build()
    }
    single { get<RunDatabase>().runDao }
    single { get<RunDatabase>().analyticsDao }
    single { get<RunDatabase>().runPendingSyncDao }
    singleOf(::RemoteLocalRunDataSource).bind<LocalRunDataSource>()
}