package com.muhammad.auth.data.di

import com.muhammad.auth.data.AuthRespositoryImp
import com.muhammad.auth.data.EmailPatternValidator
import com.muhammad.auth.domain.AuthRepository
import com.muhammad.auth.domain.PatternValidator
import com.muhammad.auth.domain.UserDataValidator
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authDataModule = module {
    single<PatternValidator>{ EmailPatternValidator }
    singleOf(::UserDataValidator)
    singleOf(::AuthRespositoryImp).bind<AuthRepository>()
}