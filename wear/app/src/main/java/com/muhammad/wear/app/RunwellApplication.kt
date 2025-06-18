package com.muhammad.wear.app

import android.app.Application
import com.muhammad.core.connectivity.data.di.connectivityModule
import com.muhammad.wear.app.di.appModule
import com.muhammad.wear.run.data.di.wearRunDataModule
import com.muhammad.wear.run.presentation.di.wearPresentationModule
import kotlinx.coroutines.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RunwellApplication : Application(){
    val applicationScope = CoroutineScope(SupervisorJob())
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@RunwellApplication)
            modules(appModule, wearPresentationModule, wearRunDataModule, connectivityModule)
        }
    }
}