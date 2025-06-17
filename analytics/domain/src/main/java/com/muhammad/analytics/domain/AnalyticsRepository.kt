package com.muhammad.analytics.domain

interface AnalyticsRepository{
    suspend fun getAnalyticsValues() : AnalyticsValues
}