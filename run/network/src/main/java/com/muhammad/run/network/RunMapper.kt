package com.muhammad.run.network

import android.annotation.SuppressLint
import com.muhammad.core.domain.location.Location
import com.muhammad.core.domain.run.Run
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("NewApi")
fun RunDto.toRun(): Run {
    return Run(
        id = id,
        maxSpeedKmh = maxSpeedKmh,
        maxHeartRate = maxHeartRate,
        mapPictureUrl = mapPictureUrl,
        totalElevationMeters = totalElevatedMeters,
        avgHeartRate = avgHeartRate,
        duration = durationMillis.milliseconds,
        dateTimeUTC = Instant.parse(dateTimeUTC).atZone(ZoneId.of("UTC")),
        distanceMeters = distanceMeters,
        location = Location(lat = lat, long = long),
    )
}

@SuppressLint("NewApi")
fun Run.toCreateRunRequest(): CreateRunRequest {
    return CreateRunRequest(
        id = id!!,
        avgHeartRate = avgHeartRate,
        avgSpeedKmh = avgSpeedKmh,
        lat = location.lat,
        long = location.long,
        maxHeartRate = maxHeartRate,
        distanceMeters = distanceMeters,
        totalElevatedMeters = totalElevationMeters,
        durationMillis = duration.inWholeSeconds,
        epochMillis = dateTimeUTC.toEpochSecond() * 1000L
    )
}
