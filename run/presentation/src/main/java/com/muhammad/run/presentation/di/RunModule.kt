package com.muhammad.run.presentation.di

import com.muhammad.run.domain.RunningTracker
import com.muhammad.run.presentation.active_run.ActiveRunViewModel
import com.muhammad.run.presentation.run_overview.RunOverviewViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
val runModule = module {
    singleOf(::RunningTracker)
    single{
        get<RunningTracker>().elapsedTime
    }
    viewModelOf(::ActiveRunViewModel)
    viewModelOf(::RunOverviewViewModel)
}