package com.muhammad.run.presentation.active_run

import com.muhammad.core.domain.location.Location
import com.muhammad.run.domain.RunData
import kotlin.time.Duration

data class ActiveRunState(
    val elapsedTime : Duration = Duration.ZERO,
    val runData : RunData = RunData(),
    val shouldTrack  : Boolean = false,
    val hasStartingRunning : Boolean = false,
    val currentLocation : Location?=null,
    val isRunFinished : Boolean = false,
    val isSavingRun : Boolean = false,
    val showLocationRationale : Boolean = false,
    val showNotificationRationale : Boolean = false
)