package com.muhammad.runwell

import android.app.Application
import android.content.Context
import com.google.android.play.core.splitcompat.SplitCompat
import com.muhammad.auth.data.di.authDataModule
import com.muhammad.auth.presentation.di.authModule
import com.muhammad.core.connectivity.data.di.connectivityModule
import com.muhammad.core.data.di.coreDataModule
import com.muhammad.core.database.di.databaseModule
import com.muhammad.run.data.di.runDataModule
import com.muhammad.run.location.di.locationModule
import com.muhammad.run.network.di.runNetworkModule
import com.muhammad.run.presentation.di.runModule
import com.muhammad.runwell.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RunwellApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    companion object {
        lateinit var INSTANCE: RunwellApplication
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        startKoin {
            androidLogger()
            androidContext(this@RunwellApplication)
            modules(
                authDataModule,
                authModule,
                appModule,
                coreDataModule,
                runModule,
                locationModule,
                databaseModule,
                runNetworkModule,
                runDataModule,
                connectivityModule
            )
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }
}