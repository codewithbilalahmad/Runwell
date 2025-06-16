package com.muhammad.core.data.di

import com.muhammad.core.data.auth.EncryptedSessionStorage
import com.muhammad.core.data.networking.HttpClientFactory
import com.muhammad.core.data.run.OfflineFirstRunRepository
import com.muhammad.core.domain.SessionStorage
import com.muhammad.core.domain.run.RunRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    single {
        HttpClientFactory(get()).build()
    }
    singleOf(::EncryptedSessionStorage).bind<SessionStorage>()
    singleOf(::OfflineFirstRunRepository).bind<RunRepository>()
}