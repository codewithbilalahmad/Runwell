package com.muhammad.auth.presentation.register

import com.muhammad.core.presentation.ui.UiText

sealed interface RegisterEvent{
    data object RegistrationSuccess : RegisterEvent
    data class Error(val error : UiText) : RegisterEvent
}