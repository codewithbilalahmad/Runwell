package com.muhammad.run.presentation.active_run.maps

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.muhammad.core.domain.location.LocationTimeStamp
import kotlin.math.abs

object PolyColorCalculator {
    fun locationsToColors(location1: LocationTimeStamp, location2: LocationTimeStamp): Color {
        val distanceMeters = location1.location.location.distanceTo(location2.location.location)
        val timeDiff =
            abs((location2.durationTimeStamp - location1.durationTimeStamp).inWholeSeconds)
        val speedKmh = (distanceMeters / timeDiff) * 3.6
        return interpolateColor(
            speedKmh = speedKmh,
            minSpeed = 5.0,
            maxSpeed = 20.0,
            colorStart = Color.Green,
           colorMid =  Color.Yellow,
            colorEnd = Color.Red
        )
    }

    private fun interpolateColor(
        speedKmh: Double,
        minSpeed: Double,
        maxSpeed: Double,
        colorStart: Color,
        colorMid: Color,
        colorEnd: Color,
    ): Color {
        val ratio = ((speedKmh - minSpeed) / (maxSpeed - minSpeed)).coerceIn(0.0..1.0)
        val colorInt = if (ratio <= 0.5) {
            val midRatio = ratio / 0.5
            ColorUtils.blendARGB(colorStart.toArgb(), colorMid.toArgb(), midRatio.toFloat())
        } else {
            val midToEnd = (ratio - 0.5) / 0.5
            ColorUtils.blendARGB(colorMid.toArgb(), colorEnd.toArgb(), midToEnd.toFloat())
        }
        return Color(colorInt)
    }
}