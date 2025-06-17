package com.muhammad.run.presentation.run_overview.mapper

import android.annotation.SuppressLint
import com.muhammad.core.domain.run.Run
import com.muhammad.core.presentation.ui.formatted
import com.muhammad.core.presentation.ui.toFormattedHeartRate
import com.muhammad.core.presentation.ui.toFormattedKmh
import com.muhammad.core.presentation.ui.toFormattedMeters
import com.muhammad.core.presentation.ui.toFormattedPace
import com.muhammad.run.presentation.run_overview.model.RunUi
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
fun Run.toRunUi(): RunUi {
    val dateTimeInLocalTime = dateTimeUTC.withZoneSameInstant(ZoneId.systemDefault())
    val formattedDateTime =
        DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mma").format(dateTimeInLocalTime)
    val distanceKm = distanceMeters / 1000.0
    return RunUi(
        id = id!!,
        dateTime = formattedDateTime,
        distance = distanceKm.toFormattedKmh(),
        avgSpeed = avgSpeedKmh.toFormattedKmh(),
        maxSpeed = maxSpeedKmh.toFormattedKmh(),
        pace = duration.toFormattedPace(distanceKm),
        totalElevation = totalElevationMeters.toFormattedMeters(),
        mapPictureUrl = mapPictureUrl,
        maxHeartRate = avgHeartRate.toFormattedHeartRate(),
        avgHeartRate = maxHeartRate.toFormattedHeartRate(), duration = duration.formatted()
    )
}