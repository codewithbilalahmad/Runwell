package com.muhammad.run.network

import kotlinx.serialization.Serializable

@Serializable
data class CreateRunRequest(
    val durationMillis : Long,
    val distanceMeters: Int,
    val epochMillis : Long,
    val lat : Double,
    val long : Double,
    val avgSpeedKmh : Double,
    val totalElevatedMeters : Int,
    val avgHeartRate : Int?,
    val maxHeartRate : Int?,
    val id : String
)