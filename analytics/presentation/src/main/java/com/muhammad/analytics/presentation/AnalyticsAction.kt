package com.muhammad.analytics.presentation

sealed interface AnalyticsAction{
    data object OnBackClick : AnalyticsAction
}