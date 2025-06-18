package com.muhammad.wear.run.data.di

import com.muhammad.wear.run.data.HealthServiceExerciseTracker
import com.muhammad.wear.run.data.WatchToPhoneConnector
import com.muhammad.wear.run.domain.ExerciseTracker
import com.muhammad.wear.run.domain.PhoneConnector
import com.muhammad.wear.run.domain.RunningTracker
import org.koin.core.module.dsl.*
import org.koin.dsl.*

val wearRunDataModule = module {
    singleOf(::HealthServiceExerciseTracker).bind<ExerciseTracker>()
    singleOf(::WatchToPhoneConnector).bind<PhoneConnector>()
    singleOf(::RunningTracker)
    single{
        get<RunningTracker>().elapsedTime
    }
}