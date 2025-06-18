package com.muhammad.wear.app.di

import com.muhammad.wear.app.RunwellApplication
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module { 
    single {
        (androidApplication() as RunwellApplication).applicationScope
    }
}