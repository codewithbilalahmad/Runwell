package com.muhammad.wear.run.presentation.di

import com.muhammad.wear.run.presentation.TrackerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val wearPresentationModule = module {
    viewModelOf(::TrackerViewModel)
}