package com.muhammad.analytics.presentation

import com.muhammad.analytics.domain.AnalyticsValues
import com.muhammad.core.presentation.ui.formatted
import com.muhammad.core.presentation.ui.toFormattedKm
import com.muhammad.core.presentation.ui.toFormattedKmh
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

fun Duration.toFormattedTotalTime(): String {
    val days = toLong(DurationUnit.DAYS)
    val hours = toLong(DurationUnit.HOURS) % 24
    val minutes = toLong(DurationUnit.MINUTES) % 60
    return "${days}d ${hours}h ${minutes}m"
}

fun AnalyticsValues.toAnalyticsDashboardState(): AnalyticsDashboardState {
    return AnalyticsDashboardState(
        totalTimeRun = totalTimeRun.toFormattedTotalTime(),
        totalDistanceRun = (totalDistanceRun / 1000.0).toFormattedKm(),
        fastestEverRun = fastestEverRun.toFormattedKmh(),
        avgDistance = (avgDistancePerRun / 1000.0).toFormattedKm(),
        avgPace = avgPacePerRun.seconds.formatted()
    )
}