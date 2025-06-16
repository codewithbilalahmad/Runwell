package com.muhammad.run.data.di

import com.muhammad.core.domain.run.SyncRunScheduler
import com.muhammad.run.data.CreateRunWorker
import com.muhammad.run.data.DeleteRunWorker
import com.muhammad.run.data.FetchRunWorker
import com.muhammad.run.data.SyncRunWorkerScheduler
import com.muhammad.run.data.connectivity.PhoneToWatchConnector
import com.muhammad.run.domain.WatchConnector
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val runDataModule = module{
    workerOf(::CreateRunWorker)
    workerOf(::FetchRunWorker)
    workerOf(::DeleteRunWorker)
    singleOf(::SyncRunWorkerScheduler).bind<SyncRunScheduler>()
    singleOf(::PhoneToWatchConnector).bind<WatchConnector>()
}