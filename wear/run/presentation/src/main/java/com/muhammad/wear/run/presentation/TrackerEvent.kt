package com.muhammad.wear.run.presentation

import com.muhammad.core.presentation.ui.UiText

sealed interface TrackerEvent{
    data object RunFinished : TrackerEvent
    data class Error(val message : UiText) : TrackerEvent
}