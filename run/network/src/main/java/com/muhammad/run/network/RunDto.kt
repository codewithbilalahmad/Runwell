package com.muhammad.run.network

import kotlinx.serialization.*

@Serializable
data class RunDto(
    val id : String,
    val dateTimeUTC : String,
    val durationMillis: Long,
    val distanceMeters : Int,
    val lat : Double,
    val long : Double,
    val avgSpeedKmh : Double,
    val maxSpeedKmh : Double,
    val totalElevatedMeters : Int,
    val mapPictureUrl : String?,
    val avgHeartRate : Int?,
    val maxHeartRate : Int?
)