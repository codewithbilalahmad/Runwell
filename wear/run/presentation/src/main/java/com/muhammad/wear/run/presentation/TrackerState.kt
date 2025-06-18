package com.muhammad.wear.run.presentation

import kotlin.time.Duration


data class TrackerState(
    val elapsedDuration : Duration = Duration.ZERO,
    val distanceMeters : Int = 0,
    val heartRate : Int = 0,
    val isTracking : Boolean = false,
    val hasStartedRunning : Boolean = false,
    val isConnectedPhoneNearBy : Boolean = false,
    val isRunActive : Boolean = false,
    val canTrackHeartRate : Boolean = false,
    val isAmbientMode : Boolean = false,
    val burnInProtectionRequired : Boolean = false
)