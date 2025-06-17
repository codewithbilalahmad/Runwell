package com.muhammad.analytics.presentation.di

import com.muhammad.analytics.presentation.AnalyticsDashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val analyticsModule = module {
    viewModelOf(::AnalyticsDashboardViewModel)
}