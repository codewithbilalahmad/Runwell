package com.muhammad.auth.presentation.register

import androidx.compose.foundation.text.input.TextFieldState
import com.muhammad.auth.domain.PasswordValidationState

data class RegisterState(
    val email : TextFieldState = TextFieldState(),
    val password : TextFieldState = TextFieldState(),
    val isEmailValid : Boolean = false,
    val isPasswordVisible : Boolean = false,
    val passwordValidationState : PasswordValidationState = PasswordValidationState(),
    val isRegistering : Boolean = false,
    val canRegister : Boolean = false
)