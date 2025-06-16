package com.muhammad.auth.presentation.di

import com.muhammad.auth.presentation.login.LoginViewModel
import com.muhammad.auth.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}