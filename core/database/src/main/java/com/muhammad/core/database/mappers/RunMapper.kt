package com.muhammad.core.database.mappers

import android.annotation.SuppressLint
import com.muhammad.core.database.entity.RunEntity
import com.muhammad.core.domain.location.Location
import com.muhammad.core.domain.run.Run
import org.bson.types.ObjectId
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("NewApi")
fun RunEntity.toRun(): Run {
    return Run(
        id = id,
        duration = durationMillis.milliseconds,
        dateTimeUTC = Instant.parse(dateTimeUTC).atZone(
            ZoneId.of("UTC")
        ),
        distanceMeters = distanceMeters,
        mapPictureUrl = mapPictureUrl,
        maxSpeedKmh = maxSpeedKmh,
        maxHeartRate = maxHeartRate, avgHeartRate = avgHeartRate, location = Location(
            lat = latitude,
            long = longitude
        ), totalElevationMeters = totalElevationMeters
    )
}

fun Run.toRunEntity(): RunEntity {
    return RunEntity(
        id = id ?: ObjectId().toHexString(),
        durationMillis = duration.inWholeMilliseconds,
        dateTimeUTC = dateTimeUTC.toString(),
        latitude = location.lat,
        longitude = location.long,
        mapPictureUrl = mapPictureUrl,
        maxSpeedKmh = maxSpeedKmh,
        maxHeartRate = maxHeartRate,
        avgHeartRate = avgHeartRate,
        avgSpeedKmh = avgSpeedKmh,
        distanceMeters = distanceMeters,
        totalElevationMeters = totalElevationMeters
    )
}