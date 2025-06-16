package com.muhammad.auth.presentation.login

import com.muhammad.core.presentation.ui.UiText

sealed interface LoginEvent{
    data class Error(val error : UiText) : LoginEvent
    data object LoginSuccess : LoginEvent
}