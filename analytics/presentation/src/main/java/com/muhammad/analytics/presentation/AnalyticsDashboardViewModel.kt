package com.muhammad.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammad.analytics.domain.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsDashboardViewModel(
    private val analyticsRepository : AnalyticsRepository
) : ViewModel(){
    private val _state= MutableStateFlow<AnalyticsDashboardState?>(null)
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch {
            _state.update {
                analyticsRepository.getAnalyticsValues().toAnalyticsDashboardState()
            }
        }
    }
}